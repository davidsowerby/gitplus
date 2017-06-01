package uk.q3c.build.gitplus.remote

import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.local.GitLocalConfiguration

/**
 *
 * A common interface for configuring a remote repo instance. Note that different [ServiceProvider]s (GitHub, BitBucket etc) may use the configuration differently.
 * See the companion [GitRemote] implementations, such as [GitHubRemote] for specifics
 *
 * Created by David Sowerby on 16 Oct 2016
 */
interface GitRemoteConfiguration {
    /**
     * If true, the remote Git instance is considered active.  Switch to false if you only want to work with the local
     * instance
     */
    var active: Boolean
    var projectDescription: String
    var projectHomePage: String
    var publicProject: Boolean
    var repoUser: String
    var repoName: String
    var issueLabels: Map<String, String>
    var mergeIssueLabels: Boolean
    var confirmDelete: String
    var create: Boolean
    var repoDeleteApprover: RemoteRepoDeleteApprover
    var providerBaseUrl: String

    fun deleteRepoApproved(): Boolean
    fun remoteRepoFullName(): String

    /**
     * If a GitLocal instance already has its origin set, this call can be used to set up some of the remote configuration from it
     */
    fun setupFromOrigin(origin: String)

    fun repoUser(remoteRepoUser: String): GitRemoteConfiguration
    fun repoName(remoteRepoName: String): GitRemoteConfiguration
    fun mergeIssueLabels(mergeIssueLabels: Boolean): GitRemoteConfiguration
    fun issueLabels(issueLabels: Map<String, String>): GitRemoteConfiguration
    fun projectDescription(projectDescription: String): GitRemoteConfiguration
    fun projectHomePage(projectHomePage: String): GitRemoteConfiguration
    fun publicProject(publicProject: Boolean): GitRemoteConfiguration
    fun confirmDelete(confirmRemoteDelete: String): GitRemoteConfiguration
    fun repoDeleteApprover(repoDeleteApprover: RemoteRepoDeleteApprover): GitRemoteConfiguration

    /**
     * If true, a remote repository is created from this configuration
     */
    fun create(value: Boolean = true): GitRemoteConfiguration

    /**
     * Copy configuration values from another instance.  Cannot use the copy method provided by a data class, because
     * that is a copy constructor, returning a new instance
     */

    fun copyFrom(other: GitRemoteConfiguration)

    fun active(value: Boolean): GitRemoteConfiguration

    /**
     * Validates configuration settings, using local configuration to replace missing values where possible and appropriate
     *
     * @throws GitPlusConfigurationException if invalid configuration found
     */
    fun validate(local: GitLocalConfiguration)

}

enum class ServiceProvider {
    GITHUB, BITBUCKET
}