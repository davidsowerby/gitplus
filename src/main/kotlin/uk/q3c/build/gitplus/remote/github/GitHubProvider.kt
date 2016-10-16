package uk.q3c.build.gitplus.remote.github

import com.jcabi.github.Github
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration

/**
 * Created by David Sowerby on 23 Oct 2016
 */
interface GitHubProvider {
    operator fun get(configuration: GitRemoteConfiguration, tokenScope: GitRemote.TokenScope): Github
    fun apiTokenRestricted(): String
    fun apiTokenCreateRepo(): String
    fun apiTokenDeleteRepo(): String
}