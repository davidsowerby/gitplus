package uk.q3c.build.gitplus.remote.github

import com.google.inject.Inject
import com.jcabi.github.Github
import com.jcabi.github.RtGithub
import uk.q3c.build.gitplus.remote.GitRemote.TokenScope
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration
import uk.q3c.build.gitplus.remote.ServiceProvider
import uk.q3c.build.gitplus.util.BuildPropertiesLoader

/**
 * Created by David Sowerby on 21 Mar 2016
 */
class DefaultGitHubProvider @Inject constructor(val propertiesLoader: BuildPropertiesLoader) : GitHubProvider {

    val serviceProvider: ServiceProvider = ServiceProvider.GITHUB

    override operator fun get(configuration: GitRemoteConfiguration, tokenScope: TokenScope): Github {
        val token: String
        when (tokenScope) {
            TokenScope.RESTRICTED -> token = apiTokenRestricted()
            TokenScope.CREATE_REPO -> token = apiTokenCreateRepo()
            TokenScope.DELETE_REPO -> token = apiTokenDeleteRepo()
        }
        return RtGithub(token)
    }

    override fun apiTokenRestricted(): String {
        return propertiesLoader.apiTokenRestricted(serviceProvider)
    }


    override fun apiTokenCreateRepo(): String {
        return propertiesLoader.apiTokenRepoCreate(serviceProvider)
    }

    override fun apiTokenDeleteRepo(): String {
        return propertiesLoader.apiTokenRepoDelete(serviceProvider)
    }
}

