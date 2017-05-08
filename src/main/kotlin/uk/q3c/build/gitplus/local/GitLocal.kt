package uk.q3c.build.gitplus.local

import com.google.common.collect.ImmutableList
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.dircache.DirCache
import uk.q3c.build.gitplus.GitSHA
import uk.q3c.build.gitplus.remote.GitRemote
import java.io.File

/**
 * Created by David Sowerby on 17 Oct 2016
 */
interface GitLocal : GitLocalConfiguration, AutoCloseable {
    val localConfiguration: GitLocalConfiguration
    var remote: GitRemote
    var git: Git

    /**
     * Equivalent to 'git init' from the command line.  Initialises `projectDir` for Git.  If a [projectCreator] has
     * been provided, it is invoked and the [projectDir] added to Git
     */
    fun init()

    /**
     * Clones the remote repo to local.  The clone url is taken from [.configuration].  If a local directory already exists which would have to be
     * overwritten (presumably because of an earlier clone), the outcome is determined by [localConfiguration.getCloneExistsResponse]:
     *  1. DELETE - deletes the local copy and clones from remote
     *  1. PULL - executes a Git 'pull' instead of a clone
     *  1. EXCEPTION - throws a GitLocalException
     *
     */
    fun cloneRemote()

    /**
     * This method should not generally be called directly .. it is invoked by [GitPlus.execute]
     *
     * Validates the [localConfiguration], then prepares this instance for the configuration settings
     * If unspecified, [projectName] is taken from the [remote.repoName] property.  If still unspecified, a validation exception is thrown
     *
     * @throws GitPlusConfigurationException
     *
     * @param the [GitRemote] associated with this local instance
     */
    fun prepare(remote: GitRemote)

    fun pull()

    /**
     * If [create] is true, creates a local Git repo, and initialises it. If [create] is false, this call does nothing
     */
    fun createAndInitialise()

    /**
     * Returns the Git hash for the currently checkout out revision

     * @return the Git hash for the currently checkout out revision
     */
    fun currentCommitHash(): String

    /**
     * Creates a new branch and checks it out.
     *
     * If [remote] is active, and the local repo has no tracking branch, a tracking branch of the same name is set,
     * and a forced push executed.
     *
     * If [remote] is active, and a tracking branch is already set, the new local branch is cheked out, but no other
     * actions are taken
     *
     * @throws GitLocalException if the checkout fails
     * @see [createBranch]
     */
    fun checkoutNewBranch(branch: GitBranch)

    /**
     * Checks out an existing branch.
     *
     * @throws GitLocalException if the checkout fails
     */
    fun checkoutBranch(branch: GitBranch)

    /**
     * Checks out a specific commit for [branch].  This may leave Git in a detached HEAD state - to avoid that, use the other
     * version of this method
     *
     * @throws GitLocalException if the checkout fails
     */
    fun checkoutCommit(sha: GitSHA)

    fun checkoutCommit(sha: GitSHA, toBranch: String)

    /**
     * Creates a new branch with the name `branchName`, but does nothing with it

     * @param branchName name of the new branch
     * *
     * @throws GitLocalException if branch cannot be created for any reason
     * @see [checkoutNewBranch]
     */
    fun createBranch(branchName: String)

    /**
     * Add file (or recursively add a directory).  JGit behaves slightly differently than command line Git.  This call will init() a repo if it has not been
     * already.

     * @param file the file to add
     *
     * @return directory cache (index) after add
     *
     * @throws GitLocalException if the action fails
     */
    fun add(file: File): DirCache

    /**
     * Executes a commit, applying [message]
     *
     * @return the commit id (SHA)
     */
    fun commit(message: String): GitSHA

    /**
     * Returns a list of branches in the repo, or an empty list if there are no branches

     * @return a list of branches in the repo, or an empty list if there are no branches
     */
    fun branches(): List<String>

    fun currentBranch(): GitBranch

    /**
     * Equivalent to 'git status' from the command line
     */
    fun status(): Status

    /**
     * Returns the URL for the remote called 'origin'.  Note that it has the '.git' suffix appended.
     * @return the URL for the remote called 'origin'.  Note that it has the '.git' suffix appended.
     *
     * @throws GitLocalException of the origin does not exist, or for any other failure
     */
    fun getOrigin(): String

    /**
     * Sets the git origin for this instance to [gitRemote]
     */
    fun setOrigin()

    /**
     * Pushes the currently checked out branch to the remote origin specified in [remote].  If a tracking branch has not been set for the
     * current branch, it is set to 'origin/<current local branch>'
     *
     * @param tags if true, tags are also pushed
     * @param force if true, the push is forced
     */
    fun push(tags: Boolean = false, force: Boolean = false): PushResponse

    /**
     * Returns all the tags from the current repo, in a slightly more digestible form than the JGit tags.  Works with either annotated or lightweight tags.
     * A lightweight tag has no tagger Ident, so it uses the committer ident

     * @return all the tags from the current repo, in a slightly more digestible form than the JGit tags.  Works with either annotated or lightweight tags.
     */
    fun tags(): List<Tag>

    /**
     * Extracts commits for the 'develop' branch. Equivalent to calling [extractCommitsFor] with branchName='develop'. The returned list is ordered with the most recent commit at index 0

     * @return commits for the 'develop' branch
     */
    fun extractDevelopCommits(): ImmutableList<GitCommit>

    /**
     * Extracts commits for the 'master' branch Equivalent to calling [extractCommitsFor] with branchName='master'. The returned list is ordered with the most recent commit at index 0

     * @return commits for the 'master' branch
     */
    fun extractMasterCommits(): ImmutableList<GitCommit>

    /**
     * Returns commits for the branch defined by [branchName].  The returned list is ordered with the most recent commit at index 0

     * @param branchName the name of the branch the commits are required for.
     * *
     * @return commits for the branch defined by [branchName]
     */
    fun extractCommitsFor(branchName: String): ImmutableList<GitCommit>

    /**
     * Returns commits for the branch defined by [branch].  The returned list is ordered with the most recent commit at index 0

     * @param branch the branch the commits are required for.
     * *
     * @return commits for the branch defined by [branchName]
     */
    fun extractCommitsFor(branch: GitBranch): ImmutableList<GitCommit>

    /**
     * Adds `tag` to the most recent commit, with [tagName] and [tagBody].  This will add an annotated tag, with
     * other attributes (such as personIdent and tagger email) taken from [localConfiguration]
     *
     * @param tagName the tag to apply
     */
    fun tag(tagName: String, tagBody: String)

    /**
     * Adds `tag` to the most recent commit.  This will add a lightweight tag.  See also [tag]

     * @param tagMsg the tag to apply
     */
    fun tagLightweight(tagMsg: String)

    /**
     * Reads the Git local information (by using the equivalent of 'git remote show origin') to get remote information.
     * Can be used with existing project instead of explicitly setting the remote url
     */
    fun verifyRemoteFromLocal()

    /**
     * Returns a [GitSHA] instance, which encapsulates the hash for the latest commit in [branch]
     */
    fun latestCommitSHA(branch: GitBranch): GitSHA

    /**
     * Returns a [GitSHA] instance, which encapsulates the hash for the latest commit in the 'develop' branch
     */
    fun latestDevelopCommitSHA(): GitSHA

    /**
     * Returns the 'develop' branch
     */
    fun developBranch(): GitBranch

    /**
     * Returns the 'master' branch
     */
    fun masterBranch(): GitBranch

    /**
     * When [prepare] is called, a check is made whether the Git repository has been init'd.  This method returns the
     * result of that check (or true if [init] has been called
     */
    fun isInitDone(): Boolean

    /**
     * Pushes a specific tag
     */
    fun pushTag(name: String): PushResponse

    /**
     * Pushes all tags
     */
    fun pushAllTags(): PushResponse
}