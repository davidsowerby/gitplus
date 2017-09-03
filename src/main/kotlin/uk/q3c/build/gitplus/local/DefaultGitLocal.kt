package uk.q3c.build.gitplus.local

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.*
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.merge.MergeStrategy
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.GitSHA
import uk.q3c.build.gitplus.gitplus.DefaultGitPlus
import uk.q3c.build.gitplus.local.Tag.TagType
import uk.q3c.build.gitplus.remote.GitRemote
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.time.ZonedDateTime
import java.util.*

/**
 * A thin wrapper around Git to make some of the commands either simpler or just more direct and relevant to the task of using within Gradle
 *
 * [branchConfigProvider] is necessary only to enable unit testing.  JGit uses a number of concrete classes directly, which
 * prevents mocking
 */
open class DefaultGitLocal @Inject constructor(
        val branchConfigProvider: BranchConfigProvider,
        val gitProvider: GitProvider,
        override val localConfiguration: GitLocalConfiguration,
        val gitInitChecker: GitInitChecker,
        val gitCloner: GitCloner)

    : GitLocal, GitLocalConfiguration by localConfiguration {


    override lateinit var git: Git
    override lateinit var remote: GitRemote
    private val log = LoggerFactory.getLogger(this.javaClass.name)

    override fun prepare(remote: GitRemote) {
        if (active) {
            log.debug("preparing GitLocal")
            this.remote = remote
            localConfiguration.validate(remote)
            git = gitProvider.openRepository(localConfiguration)
            gitInitChecker.setGit(git)
        }
    }

    override fun init() {
        log.debug("initialising {} directory for git", localConfiguration.projectDir())
        var repo: Repository? = null
        try {
            val gitDir = File(localConfiguration.projectDir(), ".git")
            repo = FileRepository(gitDir)
            repo.create()
            log.debug("repo created")
            gitInitChecker.initDone()
            projectCreator.invoke(localConfiguration)
            add(projectDir())
        } catch (e: Exception) {
            throw GitLocalException("Unable to initialise DefaultGitLocal", e)
        } finally {
            if (repo != null) {
//                close this repo instance, as another created in 'git' field
                repo.close()
            }
        }
    }


    /**
     * Closes Git instance to free up resources
     */
    override fun close() {
        try {
            git.close()
        } catch (uip: UninitializedPropertyAccessException) {
            // do nothing, if git was not initialised we do not need to close it
        }
    }

    override fun checkoutRemoteBranch(branch: GitBranch): Ref {
        try {
            val ref = git.checkout()
                    .setCreateBranch(true).setName(branch.name)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .setStartPoint("origin/${branch.name}")
                    .call()
            if (ref == null) {
                throw GitLocalException("JGit unable to checkout remote branch ${branch.name}")
            } else {
                return ref
            }
        } catch (e: Exception) {
            throw GitLocalException("Failed to checkout remote branch ${branch.name}", e)
        }
    }


    override fun cloneRemote() {
        if (cloneFromRemote) {
            log.debug("clone requested")
            try {
                val localDir = localConfiguration.projectDir()
                if (localDir.exists()) {
                    log.debug("local copy (assumed to be clone) already exists")
                    when (localConfiguration.cloneExistsResponse) {
                        CloneExistsResponse.DELETE -> {
                            //this will throw exception if denied
                            deleteFolderIfApproved(localDir)
                            doClone(localDir)
                        }

                        CloneExistsResponse.PULL -> pull()
                        CloneExistsResponse.EXCEPTION -> {
                            log.debug("Exception thrown as configured")
                            throw IOException("Git clone called, when Git local directory already exists")
                        }
                    }
                } else {
                    doClone(localDir)
                }


            } catch (e: Exception) {
                throw GitLocalException("Unable to clone " + remoteUrl(), e)
            }
        } else {
            log.debug("cloneRemote() called but ignored, as config has cloneRemote == false")
        }
    }

    private fun doClone(localDir: File) {
        gitCloner.doClone(localDir, remoteUrl(), gitInitChecker)
    }

    open fun remoteUrl(): String {
        return remote.repoBaselUrl()
    }

    private fun deleteFolderIfApproved(localDir: File) {
        if (localConfiguration.fileDeleteApprover.approve(localDir)) {
            FileUtils.forceDelete(localDir)
            log.debug("'{}' deleted", localDir)
        } else {
            log.debug("Delete of '{}' not approved", localDir)
            throw GitLocalException("Delete of directory not approved: " + localDir.absolutePath)
        }
    }

    override fun pull() {
        try {
            val pull = git.pull()
            pull.call()
        } catch (e: Exception) {
            throw GitLocalException("Pull of current branch failed", e)
        }
    }

    override fun pull(branchName: String) {
        try {
            val pull = git.pull()
            pull.remoteBranchName = branchName
            pull.call()
        } catch (e: Exception) {
            throw GitLocalException("Pull of '$branchName' branch failed", e)
        }
    }

    override fun isInitDone(): Boolean {
        return gitInitChecker.isInitDone()
    }

    override fun createAndInitialise() {
        if (create) {
            try {

                log.debug("creating local and remote repo for project: '{}'", localConfiguration.projectName)
                val projectDir = localConfiguration.projectDir()
                FileUtils.forceMkdir(projectDir)
                init()
            } catch (e: Exception) {
                throw GitLocalException("Unable to create local repo", e)
            }
        }
    }


    override fun currentCommitHash(): String {
        log.debug("Retrieving hash for currently checked out revision")
        try {
            val ref = git.repository.findRef("HEAD")
            return ref.objectId.name
        } catch (e: Exception) {
            throw GitLocalException("Unable to retrieve current revision", e)
        }

    }


    override fun checkoutNewBranch(branch: GitBranch) {
        log.info("checking out a new branch '{}'", branch)
        try {
            val checkout = git.checkout()
            checkout.setCreateBranch(true).setName(branch.name)

            if (remote.active && checkTrackingBranch()) {
                checkout.call()
                push(true, true)
                return
            }
            // if tracking branch already existed, we don't know what how it relates to local branch, just keep
            // things as they are
            checkout.call()

        } catch (e: Exception) {
            throw GitLocalException("Unable to checkout branch " + branch, e)
        }
    }


    override fun checkoutBranch(branch: GitBranch) {
        log.info("checking out existing branch '{}'", branch)
        checkInitDone()
        try {
            git.checkout().setCreateBranch(false).setName(branch.name).call()

        } catch (e: Exception) {
            throw GitLocalException("Unable to checkout branch " + branch, e)
        }
    }

    override fun checkoutCommit(sha: GitSHA) {
        log.info("checking out Git hash: '{}' ", sha)
        try {
            val checkout = git.checkout()
            checkout.setCreateBranch(false).setName(sha.sha)
            checkout.call()

        } catch (e: Exception) {
            throw GitLocalException("Unable to checkout commit " + sha, e)
        }
    }

    override fun checkoutCommit(sha: GitSHA, toBranch: String) {
        log.info("checking out Git hash: '{}' to branch '{}'", sha, toBranch)
        try {
            val checkout = git.checkout()
            checkout.setCreateBranch(true).setName(toBranch).setStartPoint(sha.sha)
            checkout.call()

        } catch (e: Exception) {
            throw GitLocalException("Unable to checkout commit " + sha, e)
        }
    }


    override fun createBranch(branchName: String) {
        log.info("creating branch '{}'", branchName)
        checkInitDone()
        try {
            git.branchCreate().setName(branchName).call()
        } catch (e: Exception) {
            throw GitLocalException("Unable to create branch " + branchName, e)
        }

    }


    override fun add(file: File): DirCache {
        try {
            checkInitDone()
            if (!file.exists()) {
                throw FileNotFoundException(file.absolutePath)
            }
            return git.add().addFilepattern(file.name).call()
        } catch (e: Exception) {
            throw GitLocalException("Unable to add file to git: " + file, e)
        }

    }

    private fun checkInitDone() {
        gitInitChecker.checkInitDone()
    }


    override fun commit(message: String): GitSHA {
        log.info("Git commit with message '$message'")
        checkInitDone()
        try {
            val commit = git.commit().setMessage(message).call()
            return GitSHA(commit.name)
        } catch (e: Exception) {
            throw GitLocalException("Unable to process Git request to commit", e)
        }

    }


    override fun branches(): List<String> {
        checkInitDone()
        try {
            val refs = git.branchList().call()
            val branchNames = ArrayList<String>()
            refs.forEach { r -> branchNames.add(r.name.replace(Constants.R_HEADS, "")) }
            return branchNames
        } catch (e: Exception) {
            throw GitLocalException("Unable to list branches ", e)
        }

    }

    override fun currentBranch(): GitBranch {
        checkInitDone()
        try {
            val branch: String? = git.repository.branch
            if (branch == null) {
                throw GitLocalException("There is no current branch")
            } else {
                return GitBranch(branch)
            }

        } catch (e: Exception) {
            throw GitLocalException("Unable to get current branch ", e)
        }

    }

    override fun status(): Status {
        try {
            return git.status().call()
        } catch (e: Exception) {
            throw GitLocalException("Git status() failed", e)
        }

    }


    override fun getOrigin(): String {
        checkInitDone()
        try {
            val config = git.repository.config
            val remotes = config.getSubsections(DefaultGitPlus.REMOTE)
            if (!remotes.contains(DefaultGitPlus.ORIGIN)) {
                throw GitLocalException("No origin has been defined for " + localConfiguration.projectDir())
            }
            return config.getString(DefaultGitPlus.REMOTE, DefaultGitPlus.ORIGIN, DefaultGitPlus.URL)
        } catch (e: Exception) {
            throw GitLocalException("Unable to get the origin", e)
        }

    }

    override fun setOrigin() {
        try {
            val originUrl = remote.cloneUrl()
            setOrigin(originUrl)
        } catch (e: Exception) {
            throw GitLocalException("Unable to set origin", e)
        }
    }

    protected fun setOrigin(origin: String) {
        try {
            val config = git.repository.config
            config.setString("remote", "origin", "url", origin)
            config.save()

        } catch (e: Exception) {
            throw GitLocalException("Unable to set origin to " + origin, e)
        }

    }


    override fun push(tags: Boolean, force: Boolean): PushResponse {
        log.info("pushing to remote, with tags='{}' and force = '{}'", tags, force)
        checkInitDone()
        try {
            if (currentBranch().name != "") {
                checkTrackingBranch()
            }

            val pc = git.push().add(currentBranch().name)

            if (tags) {
                pc.setPushTags()
            }
            pc.isForce = force
            return doPush(pc)
        } catch (e: Exception) {
            throw GitLocalException("Unable to push", e)
        }
    }

    fun doPush(pc: PushCommand): PushResponse {
        pc.setCredentialsProvider(remote.credentialsProvider)
        val results = pc.call()
        val response = PushResponse()
        for (pushResult in results) {
            response.add(pushResult)
        }
        if (response.isSuccessful) {
            return response
        }
        throw PushException(response)
    }

    override fun pushTag(name: String): PushResponse {
        log.info("pushing tag {}", name)
        checkInitDone()
        val refSpec = "refs/tags/$name:refs/tags/$name"
        val pc = git.push().add(refSpec)
        return doPush(pc)
    }

    override fun pushAllTags(): PushResponse {
        log.info("pushing all tags {}")
        checkInitDone()
        val refSpec = "refs/tags/*:refs/tags/*"
        val pc = git.push().add(refSpec)
        return doPush(pc)
    }

    /**
     * Checks whether current branch has a tracking branch set, and sets one (with the same branch name as local) if not
     *
     * @return true if a new tracking branch set up
     */
    private fun checkTrackingBranch(): Boolean {
        if (remote.active) {
            val cfg = git.repository.config
            val branch = git.repository.branch
            val trackingBranch: String? = branchConfigProvider.get(cfg, branch).trackingBranch

            if (trackingBranch == null) {
                val checkout = git.checkout()
                val currentBranch = currentBranch()
                checkout.setName(currentBranch.name).setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM).setStartPoint(currentBranch.name).call()
                return true
            }
        }
        return false
    }


    override fun tags(): List<Tag> {
        checkInitDone()
        val tags = ArrayList<Tag>()
        try {
            val refs = git.tagList().call()
            val repo = git.repository
            val walk = RevWalk(repo)
            for (ref in refs) {
                val revObject = walk.parseAny(ref.objectId)
                val tagName = ref.name.replace("refs/tags/", "")
                val tagReleaseDate: ZonedDateTime
                val tagCommitDate: ZonedDateTime
                val fullMessage: String
                val taggerIdent: PersonIdent
                val tagType: TagType
                val commit: GitCommit
                if (revObject is RevTag) {
                    tagReleaseDate = extractDateFromIdent(revObject.taggerIdent)
                    val codeCommit = revObject.`object` as RevCommit
                    walk.parseCommit(codeCommit)
                    tagCommitDate = extractDateFromIdent(codeCommit.committerIdent)
                    taggerIdent = revObject.taggerIdent
                    fullMessage = revObject.fullMessage
                    tagType = TagType.ANNOTATED
                    commit = GitCommit(codeCommit)


                } else {
                    val revCommit = revObject as RevCommit
                    tagReleaseDate = extractDateFromIdent(revCommit.committerIdent)
                    tagCommitDate = extractDateFromIdent(revCommit.committerIdent)
                    fullMessage = ""
                    taggerIdent = revCommit.committerIdent
                    tagType = TagType.LIGHTWEIGHT
                    commit = GitCommit(revCommit)
                }
                val tag = Tag(tagName, tagReleaseDate, tagCommitDate, taggerIdent, fullMessage, commit, tagType)
                tags.add(tag)
            }
            return tags
        } catch (e: Exception) {
            throw GitLocalException("Unable to read Git status" + localConfiguration.projectDir().absolutePath, e)
        }

    }


    private fun extractDateFromIdent(personIdent: PersonIdent): ZonedDateTime {
        return personIdent.`when`.toInstant().atZone(personIdent.timeZone.toZoneId())
    }


    override fun extractCommitsFor(branch: GitBranch): ImmutableList<GitCommit> {
        log.debug("Retrieving commits for '{}' branch", branch.name)
        try {
            val repo = git.repository
            val walk = RevWalk(repo)
            val ref1 = repo.findRef(branch.name)
            val headCommit = walk.parseCommit(ref1.objectId) // TODO This throws NPE if HEAD detached - needs a better error message
            val allCommits = git.log().all().call()
            val branchCommits = LinkedHashSet<GitCommit>()
            for (commit in allCommits) {
                val candidate = walk.parseCommit(commit)
                if (walk.isMergedInto(candidate, headCommit)) {
                    walk.parseBody(candidate)
                    val gitCommit = GitCommit(commit)
                    branchCommits.add(gitCommit)
                }
            }
            log.debug("Total of {} commits found", branchCommits.size)
            return ImmutableList.copyOf(branchCommits)
        } catch (e: Exception) {
            throw GitLocalException("Reading commits for branch '${branch.name}' failed", e)
        }
    }

    override fun headDevelopCommitSHA(): GitSHA {
        return headCommitSHA(developBranch())
    }

    override fun headCommitSHA(branch: GitBranch): GitSHA {
        val b = git.repository.findRef(branch.name) ?: throw GitLocalException("There is no '$branch' branch")
        val commitId = b.objectId.name
        log.debug("HEAD commit for '{}' branch is {}", branch.name, commitId)
        return GitSHA(commitId)
    }


    override fun extractDevelopCommits(): ImmutableList<GitCommit> {
        return extractCommitsFor(DefaultGitPlus.DEVELOP_BRANCH)
    }


    override fun extractMasterCommits(): ImmutableList<GitCommit> {
        return extractCommitsFor(DefaultGitPlus.MASTER_BRANCH)
    }


    override fun extractCommitsFor(branchName: String): ImmutableList<GitCommit> {
        return extractCommitsFor(GitBranch(branchName))
    }

    override fun tag(tagName: String, tagBody: String) {
        try {
            val tagCommand = git.tag()
            val tagDate = Date()
            val personalIdent = PersonIdent(localConfiguration.taggerName, localConfiguration.taggerEmail)
            tagCommand.setMessage(tagBody).setAnnotated(true).setName(tagName).tagger = PersonIdent(personalIdent, tagDate)
            tagCommand.call()

        } catch (e: Exception) {
            throw GitLocalException("Unable to tag" + localConfiguration.projectDir().absolutePath, e)
        }

    }


    override fun tagLightweight(tagMsg: String) {
        try {
            val tagCommand = git.tag()
            tagCommand.setAnnotated(false).setName(tagMsg).call()
        } catch (e: Exception) {
            throw GitLocalException("Unable to tag" + localConfiguration.projectDir().absolutePath, e)
        }

    }

    override fun developBranch(): GitBranch {
        return GitBranch("develop")
    }

    override fun masterBranch(): GitBranch {
        return GitBranch("master")
    }

    override fun mergeBranch(branch: GitBranch, strategy: MergeStrategy, fastForward: MergeCommand.FastForwardMode): MergeResult {
        val mergeFailMsg: String
        try {
            val mergeCommand = git.merge()
            val ref1 = git.repository.findRef(branch.name)
            mergeCommand.include(ref1)
            mergeCommand.setStrategy(strategy)
            val mergeResult: MergeResult = mergeCommand.call()
            if (mergeResult.mergeStatus.isSuccessful) {
                return mergeResult
            }
            mergeFailMsg = mergeResult.toString()

        } catch (e: Exception) {
            throw GitLocalException("Exception thrown during merge", e)
        }
        // mergeResult is not successful
        throw GitLocalException("Merge unsuccessful:\n" + mergeFailMsg)
    }

}


