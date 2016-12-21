package uk.q3c.build.gitplus.remote

import com.google.inject.Provider

/**
 * Resolves to a [GitRemote] instance with configuration prepared.  The selection of remote type is provided by [remotes], a map
 * defined by [GitPlusModule]
 *
 * Created by David Sowerby on 25 Oct 2016
 */
interface GitRemoteResolver {
    val remotes: MutableMap<ServiceProvider, Provider<GitRemote>>

    /**
     * Returns a [GitRemote] instance for the [ServiceProvider].  The configuration of the instance is copied from that
     * provided here
     */
    fun get(serviceProvider: ServiceProvider, configuration: GitRemoteConfiguration): GitRemote

    /**
     * Returns a [GitRemote] instance for the [defaultProvider]
     */
    fun getDefault(): GitRemote

    /**
     * Declares the default [ServiceProvider] to use
     */
    fun defaultProvider(): ServiceProvider
}