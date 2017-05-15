package uk.q3c.build.gitplus.remote.github

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.jcabi.github.*
import com.jcabi.http.Request
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.GitSHA
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.remote.*
import uk.q3c.build.gitplus.remote.GitRemote.TokenScope.*
import java.io.IOException
import java.util.*

/**
 * Created by David Sowerby on 12 Feb 2016
 */
class DefaultGitHubRemote @Inject constructor(override var configuration: GitRemoteConfiguration, val gitHubProvider: GitHubProvider, val remoteRequest: RemoteRequest, override val urlMapper: GitHubUrlMapper) :
        GitHubRemote,
        GitRemoteConfiguration by configuration,
        GitRemoteUrlMapper by urlMapper {


    override fun hasBranch(branch: GitBranch): Boolean {
        try {
            getBranch(branch)
            return true
        } catch(e: Exception) {
            return false
        }
    }


    enum class Status {
        GREEN, YELLOW, RED
    }

    private val log = LoggerFactory.getLogger(this.javaClass.name)
    private var gitHub: Github = getGitHub(RESTRICTED)
    private var currentTokenScope: GitRemote.TokenScope = RESTRICTED

    init {
        urlMapper.parent = this
    }


    override fun isIssueFixWord(word: String): Boolean {
        return fixWords.contains(word.toLowerCase())
    }


    override fun getIssue(issueNumber: Int): GPIssue {
        return getIssue(configuration.repoUser, configuration.repoName, issueNumber)
    }

    override fun getIssue(remoteRepoUser: String, remoteRepoName: String, issueNumber: Int): GPIssue {
        log.debug("Retrieving issue {} from {}", issueNumber, remoteRepoName)
        try {
            val repo = getGitHub(RESTRICTED).repos().get(Coordinates.Simple(remoteRepoUser, remoteRepoName))
            return GPIssue(repo.issues().get(issueNumber))
        } catch (e: Exception) {
            throw GitRemoteException("Unable to retrieve issue $issueNumber from $remoteRepoName", e)
        }

    }

    override val credentialsProvider: CredentialsProvider
        get() {
            try {
                val token = gitHubProvider.apiTokenRestricted()
                return UsernamePasswordCredentialsProvider(token, "")
            } catch (e: Exception) {
                throw GitRemoteException("An api token is required in order to enable credentials", e)
            }

        }

    override fun apiStatus(): Status {

        try {
            val token = gitHubProvider.apiTokenRestricted()

            val response = remoteRequest.request(Request.GET, GitHubUrlMapper.STATUS_API_URL, token)
            val status = response.readObject().getString("status")
            when (status) {
                "good" -> return Status.GREEN
                "minor" -> return Status.YELLOW
                "major" -> return Status.RED
                else -> return Status.RED
            }
        } catch (e: Exception) {
            log.error("Unable to retrieve status from online service", e)
            return Status.RED
        }

    }


    override fun createIssue(issueTitle: String, body: String, vararg labels: String): GPIssue {
        val issue = getRepo(RESTRICTED).issues().create(issueTitle, body)

        val jsIssue = Issue.Smart(issue)
        jsIssue.assign(configuration.repoUser)
        jsIssue.labels().add(ImmutableList.copyOf(labels))
        return GPIssue(jsIssue)
    }

    private fun getGitHub(tokenScope: GitRemote.TokenScope): Github {
        if (currentTokenScope != tokenScope) {
            this.gitHub = gitHubProvider.get(configuration, tokenScope)
            currentTokenScope = tokenScope
        }
        return gitHub
    }


    fun getRepo(tokenScope: GitRemote.TokenScope): Repo {
        val repos = getGitHub(tokenScope).repos()
        return repos.get(Coordinates.Simple(configuration.repoUser, configuration.repoName))
    }

    /**
     * Creates the remote repo from the information in [.configuration].  Note that there is no setting
     * for any option to control creation of a wiki - it is always created, even if [configuration] has useWiki set to false.  That said, you still
     * wll not be able to access the wiki via the API until you manually create the first page.
     */
    override fun createRepo() {
        try {
            val settings = Repos.RepoCreate(configuration.repoName, !configuration.publicProject)
            settings.withDescription(configuration.projectDescription).withHomepage(configuration.projectHomePage)
            getGitHub(CREATE_REPO).repos().create(settings)
            if (configuration.mergeIssueLabels) {
                mergeLabels()
            }
        } catch (e: Exception) {
            throw GitRemoteException("Unable to create Repo", e)
        }

    }


    override fun deleteRepo() {
        try {
            if (configuration.deleteRepoApproved()) {
                getGitHub(DELETE_REPO).repos().remove(Coordinates.Simple(configuration.repoUser, configuration.repoName))
            } else {
                throw GitRemoteException("Repo deletion not confirmed")
            }
        } catch (e: Exception) {
            throw GitRemoteException("Unable to delete remote repo", e)
        }

    }


    override fun listRepositoryNames(): Set<String> {
        val qualifier = "user:" + getRepo(RESTRICTED).coordinates().user()
        val repoNames = TreeSet<String>()
        val repos = getGitHub(RESTRICTED).search().repos(qualifier, "coordinates", Search.Order.ASC)
        repos.iterator().forEach { r -> repoNames.add(r.coordinates().repo()) }
        return repoNames
    }


    override fun mergeLabels() {
        mergeLabels(configuration.issueLabels)
    }


    override fun mergeLabels(labelsToMerge: Map<String, String>): Map<String, String> {
        val labels = getRepo(RESTRICTED).labels()
        val iterate = labels.iterate()
        val iterator = iterate.iterator()

        //copy of map, we need to remove entries to keep track, and we may have been passed an ImmutableMap
        val lblsToMerge = TreeMap(labelsToMerge)

        //take existing labels first, update any colours which have changed, and remove labels that are not in the 'labelsToMerge'
        val existingToRemove = ArrayList<Label>()
        while (iterator.hasNext()) {
            val currentLabel = iterator.next()
            val currentName = currentLabel.name()
            //if this repo label is in new set as well, update the colour if, but only if not already the same (save unnecessary API calls)
            if (labelsToMerge.containsKey(currentName)) {
                val mergeColour = labelsToMerge[currentName]
                val smLabel = Label.Smart(currentLabel)
                if (smLabel.color() != mergeColour) {
                    smLabel.color(mergeColour)// this does the update (patch)
                }
                //processed, remove from merge set
                lblsToMerge.remove(currentName)
            } else {
                // it is not in the new set, note it for removal
                existingToRemove.add(currentLabel)
            }
        }

        //remove the unwanted
        for (label in existingToRemove) {
            log.debug("deleting label '{}", label.name())
            labels.delete(label.name())
        }

        //existing set is updated except for any new ones that need to be added
        //lblsToMerge now contains only those which have not been processed
        for ((key, value) in lblsToMerge) {
            try {
                labels.create(key, value)
            } catch (ioe: IOException) {
                log.warn("Failed to add new label '{}'", key, ioe)
            }

        }

        // do final check to make sure the two sets are the same, if different throw exception
        val checkMap = labelsAsMap

        if (checkMap != labelsToMerge) {
            throw GitRemoteException("Labels did not merge correctly")
        }
        return checkMap
    }


    override val labelsAsMap: Map<String, String>
        get() {
            val labels = getRepo(RESTRICTED).labels()
            val map = TreeMap<String, String>()
            for (label in labels.iterate()) {
                val smLabel = Label.Smart(label)
                map.put(smLabel.name(), smLabel.color())
            }
            return map
        }

    override fun latestCommitSHA(branch: GitBranch): GitSHA {
        val developBranch = getBranch(branch)
        return GitSHA(developBranch.commit().sha())
    }

    override fun latestDevelopCommitSHA(): GitSHA {
        return latestCommitSHA(GitBranch("develop"))
    }

    override fun prepare(local: GitLocal) {
        if (active) {
            validate(local)
        }
    }


    private fun getBranch(branch: GitBranch): Branch {
        val branchIterator = getRepo(RESTRICTED).branches().iterate().iterator()
        while (branchIterator.hasNext()) {
            val candidate = branchIterator.next()
            if (candidate.name() == branch.name) {
                return candidate
            }
        }
        throw IOException("${branch.name} not found")
    }

    companion object {

        private val fixWords = ImmutableList.of("fix", "fixes", "fixed", "resolve", "resolves", "resolved", "close", "closes",
                "closed")
    }


}


