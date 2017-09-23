package uk.q3c.build.gitplus.gitplus

import uk.q3c.build.gitplus.local.*
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration
import uk.q3c.build.gitplus.remote.ServiceProvider
import uk.q3c.build.gitplus.util.PropertiesResolver
import java.io.File

/**
 * Brings together
 *  1. a [GitLocal] instance representing a local Git repo,
 *  1. a [GitRemote] instance representing a remote, hosted Git with issues
 *  1. a [WikiLocal] instance to represent a wiki repo associated with the main code repo.  This may not apply to all remote service
 * providers, but there is currently no option to remove it
 *
 * To configure, call the configuration methods provided by [GitPlus], for example [cloneFromRemote] or set the properties of [GitRemoteConfiguration] and [GitLocalConfiguration] directly
 * These interfaces are delegates within [GitRemote], [GitLocal]. [WikiLocal] delegates to its own [GitLocalConfiguration].
 *
 * For example, to set up the remote repo user name and repo name:
 *
 * `gitPlus.remote.repoUser("davidsowerby").repoName("krail")`
 *
 *
 * Once the configuration has been set up, invoke it by calling [execute]
 *
 *
 * Created by David Sowerby on 16 Oct 2016
 */

interface GitPlus : PropertiesResolver, AutoCloseable, GitPlusConfiguration {
    var serviceProvider: ServiceProvider
    val local: GitLocal
    val wikiLocal: WikiLocal
    val configuration: GitPlusConfiguration
    val urlParser: UrlParser

    /**
     * The [GitRemote] instance is selected by configuration, so we use a provider to set this property
     */
    var remote: GitRemote

    /**
     * Checks configuration and sets defaults where possible.  Usually only called directly by [execute] or for testing
     *
     * @throw various exceptions if configuration is incomplete or inconsistent
     */
    fun evaluate()

    /**
     * The main entry point.  Executes the settings provided via configuration, creating / cloning or otherwise manipulating repositories as required.

     * @throws GitPlusException if anything fails
     */
    fun execute(): GitPlus

    /**
     * Convenience configuration method, sets up local and remote configuration to clone a remote repo, with the option to clone
     * the associated wiki as well.
     *
     * @param cloneParentDir the local parent directory for the clone
     * @param remoteRepoUserName the user name element of the remote repo, for example 'davidsowerby' in 'davidsowerby/krail'
     * @param projectName used as the directory name for the clone and also the remote project name, for example 'krail' in 'davidsowerby/krail'
     * @param includeWiki true to clone the wiki as well
     */
    fun cloneFromRemote(cloneParentDir: File, remoteRepoUserName: String, projectName: String, includeWiki: Boolean)

    /**
     *
     * Convenience configuration method, sets up local and remote configuration to clone a remote repo, with the option to clone
     * the associated wiki as well.
     *
     * @param cloneParentDir the local parent directory for the clone
     * @param remoteRepoUserName the user name element of the remote repo, for example 'davidsowerby' in 'davidsowerby/krail'
     * @param projectName used as the directory name for the clone and also the remote project name, for example 'krail' in 'davidsowerby/krail'
     * @param includeWiki true to clone the wiki as well
     */
    fun cloneFromRemote(cloneParentDir: File, remoteRepoUserName: String, projectName: String, includeWiki: Boolean, cloneExistsResponse: CloneExistsResponse)

    /**
     * Convenience configuration method, sets up local and remote configuration to create a project in a local directory, create a remote repo, and pushes the local copy to remote.
     * You will need the appropriate API key set up, see http://gitplus.readthedocs.io/en/stable/build-properties/
     *
     * Other options to consider, set via gitPlus.local, gitPlus.remote, or gitPlus.wikiLocal as appropriate, see also http://gitplus.readthedocs.io/en/stable/gitplus-configuration/
     *
     * [GitLocalConfiguration.projectCreator] or use the other method
     * [GitLocalConfiguration.taggerEmail]
     * [GitLocalConfiguration.taggerName]
     * [GitRemoteConfiguration.issueLabels]
     * [GitRemoteConfiguration.publicProject]
     * [GitRemoteConfiguration.projectDescription]
     *
     * @param parentDir the local parent directory in which to create the project
     * @param remoteRepoUserName the user name element of the remote repo, for example 'davidsowerby' in 'davidsowerby/krail'
     * @param projectName used as the directory name for the project and also the remote project name, for example 'krail' in 'davidsowerby/krail'
     * @param includeWiki true to create the wiki as well - be aware that GitHub requires manual intervention to create a wiki page online before the remote wiki becomes available
     * @param publicRepo true if the remote repo is public, false if private
     */
    fun createLocalAndRemote(parentDir: File, remoteRepoUserName: String, projectName: String, includeWiki: Boolean, publicRepo: Boolean)

    /**
     * Convenience configuration method, sets up local and remote configuration to create a project in a local directory, create a remote repo, and pushes the local copy to remote
     *  You will need the appropriate API key set up, see http://gitplus.readthedocs.io/en/stable/build-properties/
     *
     * Other options to consider, set via gitPlus.local, gitPlus.remote, or gitPlus.wikiLocal as appropriate, see also http://gitplus.readthedocs.io/en/stable/gitplus-configuration/
     *
     * [GitLocalConfiguration.taggerEmail]
     * [GitLocalConfiguration.taggerName]
     * [GitRemoteConfiguration.issueLabels]
     * [GitRemoteConfiguration.publicProject]
     * [GitRemoteConfiguration.projectDescription]
     * [GitRemoteConfiguration.projectHomePage]
     *
     * @param parentDir the local parent directory in which to create the project
     * @param remoteRepoUserName the user name element of the remote repo, for example 'davidsowerby' in 'davidsowerby/krail'
     * @param projectName used as the directory name for the project and also the remote project name, for example 'krail' in 'davidsowerby/krail'
     * @param includeWiki true to create the wiki as well - be aware that GitHub requires manual intervention to create a wiki page online before the remote wiki becomes available
     *      * @param publicRepo true if the remote repo is public, false if private
     * @param projectCreator an implementation of [ProjectCreator], which is called after directories have been created, allowing a custom project to be built. <br>
     * See also this related [project](https://github.com/davidsowerby/projectadmin)
     */
    fun createLocalAndRemote(parentDir: File, remoteRepoUserName: String, projectName: String, includeWiki: Boolean, publicRepo: Boolean, projectCreator: ProjectCreator)

    /**
     * Use an existing remote repo only
     *
     * Other options to consider, set via gitPlus.remote, see also http://gitplus.readthedocs.io/en/stable/gitplus-configuration/
     *
     * [GitRemoteConfiguration.issueLabels]
     *
     * @param remoteRepoUserName the user name element of the remote repo, for example 'davidsowerby' in 'davidsowerby/krail'
     * @param projectName the remote project name, for example 'krail' in 'davidsowerby/krail'
     */
    fun useRemoteOnly(remoteRepoUserName: String, projectName: String)

    /**
     * Just create a remote repo, no relationship with anything local
     *
     * Other options to consider, set via gitPlus.remote, see also http://gitplus.readthedocs.io/en/stable/gitplus-configuration/
     *
     * [GitRemoteConfiguration.issueLabels]
     * [GitRemoteConfiguration.projectDescription]
     * [GitRemoteConfiguration.projectHomePage]
     *
     * @param remoteRepoUserName the user name element of the remote repo, for example 'davidsowerby' in 'davidsowerby/krail'
     * @param projectName the remote project name, for example 'krail' in 'davidsowerby/krail'
     * @param publicProject if true create as a public project, if false, create as a private project (both will need appropriate API keys, see http://gitplus.readthedocs.io/en/stable/build-properties/)
     */
    fun createRemoteOnly(remoteRepoUserName: String, projectName: String, publicProject: Boolean)

    /**
     * Sets up configuration so that properties are taken from gradle.properties. Not effective until [execute] is called
     */
    fun propertiesFromGradle(): GitPlus

    /**
     * Sets up configuration so that properties are taken from gitplus.properties. Not effective until [execute] is called
     */
    fun propertiesFromGitPlus(): GitPlus
}