package uk.q3c.build.gitplus.remote.github

import com.jcabi.github.Github
import com.jcabi.github.RtGithub
import com.jcabi.http.wire.RetryWire
import uk.q3c.build.gitplus.remote.GitRemote.TokenScope
import uk.q3c.build.gitplus.remote.ServiceProvider
import uk.q3c.build.gitplus.util.PropertiesResolver


/**
 * Created by David Sowerby on 21 Mar 2016
 */

class DefaultGitHubProvider : GitHubProvider {

    override fun get(propertiesResolver: PropertiesResolver, tokenScope: TokenScope): Github {
        when (tokenScope) {
            TokenScope.CREATE_ISSUE -> return create(propertiesResolver.apiTokenIssueCreate(ServiceProvider.GITHUB))
            TokenScope.CREATE_REPO -> return create(propertiesResolver.apiTokenRepoCreate(ServiceProvider.GITHUB))
            TokenScope.DELETE_REPO -> return create(propertiesResolver.apiTokenRepoDelete(ServiceProvider.GITHUB))
        }
    }

    fun create(oauthKey: String): RtGithub {
        return RtGithub(
                RtGithub(oauthKey).entry().through(RetryWire::class.java)
        )
    }


}

