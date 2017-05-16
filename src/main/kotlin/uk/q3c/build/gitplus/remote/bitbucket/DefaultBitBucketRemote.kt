package uk.q3c.build.gitplus.remote.bitbucket

import com.google.inject.Inject
import org.eclipse.jgit.transport.CredentialsProvider
import uk.q3c.build.gitplus.GitSHA
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.remote.GPIssue
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration
import uk.q3c.build.gitplus.remote.GitRemoteUrlMapper
import uk.q3c.build.gitplus.remote.RemoteRequest
import uk.q3c.build.gitplus.remote.github.DefaultGitHubRemote

/**
 * Created by David Sowerby on 25 Oct 2016
 */
class DefaultBitBucketRemote @Inject constructor(override val configuration: GitRemoteConfiguration, val bitBucketProvider: BitBucketProvider, val remoteRequest: RemoteRequest, override val urlMapper: BitBucketUrlMapper) :
        BitBucketRemote,
        GitRemoteConfiguration by configuration, GitRemoteUrlMapper by urlMapper {


    override fun prepare(local: GitLocal) {
        TODO()
//        validate()
    }

    override fun hasBranch(branch: GitBranch): Boolean {
        TODO()
    }

    override fun latestCommitSHA(branch: GitBranch): GitSHA {
        TODO()
    }

    override fun isIssueFixWord(word: String): Boolean {
        TODO()
    }

    override fun getIssue(issueNumber: Int): GPIssue {
        TODO()
    }

    override fun getIssue(remoteRepoUser: String, remoteRepoName: String, issueNumber: Int): GPIssue {
        TODO()
    }

    override val credentialsProvider: CredentialsProvider
        get() = throw NotImplementedError()

    override fun apiStatus(): DefaultGitHubRemote.Status {
        TODO()
    }

    override fun createIssue(issueTitle: String, body: String, vararg labels: String): GPIssue {
        TODO()
    }

    override fun createRepo() {
        TODO()
    }

    override fun deleteRepo() {
        TODO()
    }

    override fun listRepositoryNames(): Set<String> {
        TODO()
    }

    override fun mergeLabels() {
        TODO()
    }

    override fun mergeLabels(labelsToMerge: Map<String, String>): Map<String, String> {
        TODO()
    }

    override val labelsAsMap: Map<String, String>
        get() = throw NotImplementedError()

    override fun latestDevelopCommitSHA(): GitSHA {
        TODO()
    }
}