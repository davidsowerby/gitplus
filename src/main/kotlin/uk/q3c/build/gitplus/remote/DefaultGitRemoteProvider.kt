package uk.q3c.build.gitplus.remote

import com.google.inject.Inject
import com.google.inject.Provider

/**
 * Created by David Sowerby on 25 Oct 2016
 */
class DefaultGitRemoteProvider @Inject constructor(override val remotes: MutableMap<ServiceProvider, Provider<GitRemote>>) : GitRemoteProvider {

    override fun get(serviceProvider: ServiceProvider, configuration: GitRemoteConfiguration): GitRemote {
        // this should never actually return null provided all ServiceProviders have an entry in the map
        // map defined in GitPlusModule
        val remote = remotes.get(serviceProvider)!!.get()
        // this is a reference not a copy, because we want any further changes to be available to the remote instance as well
        remote.configuration.copy(configuration)
        return remote
    }
}