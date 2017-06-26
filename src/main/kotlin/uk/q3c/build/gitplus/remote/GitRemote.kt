package uk.q3c.build.gitplus.remote

import org.eclipse.jgit.transport.CredentialsProvider
import uk.q3c.build.gitplus.GitSHA
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.remote.github.DefaultGitHubRemote
import java.io.IOException

/**
 * Created by David Sowerby on 08 Mar 2016
 */
interface GitRemote : GitRemoteConfiguration, GitRemoteUrlMapper {

    val configuration: GitRemoteConfiguration
    val urlMapper: GitRemoteUrlMapper

    /**
     * Reference to the related local Git instance.  Will not be initialised until [prepare] has been called (invoked by
     * [GitPlus.execute])
     */
    var local: GitLocal

    enum class TokenScope {
        RESTRICTED, CREATE_REPO, DELETE_REPO
    }

    fun isIssueFixWord(word: String): Boolean


    /**
     * Returns a representation of the issue from the current remote repo (as defined by [GitRemoteConfiguration.repoUser]
     * and [GitRemoteConfiguration.repoName]
     *
     * @param issueNumber the issue number to get
     * @return the issue for the given number
     * @throws GitRemoteException if the issue cannot be retrieved for any reason
     */
    fun getIssue(issueNumber: Int): GPIssue

    /**
     * Note that this returns a [GPIssue], which contains only a subset of the information available - this is intended for use with the changelog.  You
     * can access the full issue implementation by accessing the underlying repository.  If `repoName` is null or empty the current repo is assumed
     *

     * @param remoteRepoUser user name for the repo to get the issue from
     * @param remoteRepoName repo name for the repo to get the issue from
     * @param issueNumber    the issue number to get
     * @return the issue for the given number
     *
     * @throws GitRemoteException if the issue cannot be retrieved for any reason
     */
    fun getIssue(remoteRepoUser: String, remoteRepoName: String, issueNumber: Int): GPIssue

    val credentialsProvider: CredentialsProvider

    fun apiStatus(): DefaultGitHubRemote.Status

    fun createIssue(issueTitle: String, body: String, vararg labels: String): GPIssue

    /**
     * Creates a repo with information provided by configuration.  Requires the appropriate API token
     */
    fun createRepo()

    /**
     * Deletes the repo identified by configuration. Requires the appropriate API token
     */
    fun deleteRepo()

    fun listRepositoryNames(): Set<String>

    /**
     * Merges the issue labels of the current repository with those defined by [GitRemoteConfiguration],  The equivalent of calling
     * [mergeLabels] with [GitRemoteConfiguration.getIssueLabels]
     */
    fun mergeLabels()

    /**
     * Merges the issue labels of the current repository with those defined by [GitRemoteConfiguration].  'Merge' means:
     *  1. if a label in the current repository has the same name as one in `labelsToMerge`, the current label colour is
     *  updated to that in `labelsToMerge` - this is therefore just a colour change
     *  1. If a label does not exist in the current repository, it is added
     *  1. If a label exists in the current repository, but not in `labelsToMerge`, it is removed. (And will therefore be removed from any issues it is
     * attached to)
     *
     */
    fun mergeLabels(labelsToMerge: Map<String, String>): Map<String, String>

    /**
     * Returns issue labels as a map where (K,V) = (label name, label colour)
     *
     * @return issue labels as a map where (K,V) = (label name, label colour)
     *
     *
     * @throws IOException
     */
    val labelsAsMap: Map<String, String>

    /**
     * Returns the hash for the HEAD commit on the develop branch.  Exactly the same as calling [headCommit] with branchName of 'develop'
     */
    fun developHeadCommit(): GitSHA

    /**
     * Returns the HEAD commit (SHA) for [branch]
     */
    fun headCommit(branch: GitBranch): GitSHA

    /**
     * Returns true if the remote contains a branch with name [branch.name]
     */
    fun hasBranch(branch: GitBranch): Boolean

    fun prepare(local: GitLocal)

    /**
     * Reads the Git local information (by using the equivalent of 'git remote show origin') to get remote information,
     * instead of explicitly setting the remote url.  Will fail configuration if local is not active
     */
    fun verifyFromLocal()




}
