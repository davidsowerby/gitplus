package uk.q3c.build.gitplus.remote

import com.google.inject.Inject
import com.google.inject.Provider

/**
 * Created by David Sowerby on 25 Oct 2016
 */
class DefaultGitRemoteResolver @Inject constructor(override val remotes: MutableMap<ServiceProvider, Provider<GitRemote>>) : GitRemoteResolver {

    override fun defaultProvider(): ServiceProvider {
        return ServiceProvider.GITHUB
    }

    override fun get(serviceProvider: ServiceProvider, configuration: GitRemoteConfiguration): GitRemote {
        // this should never actually return null provided all ServiceProviders have an entry in the map
        // map defined in GitPlusModule
        val remote = remotes.get(serviceProvider)!!.get()
        // copy any changes which have been made to the configuration
        remote.configuration.copy(configuration)
        return remote
    }

    override fun getDefault(): GitRemote {
        return remotes.get(ServiceProvider.GITHUB)!!.get()
    }
}