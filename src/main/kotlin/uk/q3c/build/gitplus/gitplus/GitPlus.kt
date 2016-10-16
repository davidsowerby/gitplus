package uk.q3c.build.gitplus.gitplus

import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.local.WikiLocal
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.ServiceProvider

/**
 * Brings together
 *  1. a [GitLocal] instance representing a local Git repo,
 *  1. a [GitRemote] instance representing a remote, hosted Git with issues
 *  1. a [WikiLocal] instance to represent a wiki repo associated with the main code repo.  This may not apply to all remote service
 * providers, but there is currently no option to remove it
 *
 * To configure, call the configuration methods provided by [GitRemoteConfiguration], [GitLocalConfiguration] and [WikiLocalConfiguration]
 * These interfaces are all delegates within [GitRemote], [GitLocal] and [WikiLocal] respectively.
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

interface GitPlus : AutoCloseable {
    var serviceProvider: ServiceProvider
    val local: GitLocal
    val wikiLocal: WikiLocal

    /**
     * The [GitRemote] instance is selected by configuration, so we use a provider to set this property
     */
    var remote: GitRemote

    /**
     * The main entry point.  Executes the settings provided via configuration, creating / cloning or otherwise manipulating repositories as required

     * @throws GitPlusException if anything fails
     */
    fun execute(): GitPlus
}