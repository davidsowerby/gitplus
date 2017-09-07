package uk.q3c.build.gitplus.remote.github

import com.jcabi.github.Github
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.util.PropertiesResolver

/**
 * Created by David Sowerby on 23 Oct 2016
 */
interface GitHubProvider {
    fun get(propertiesResolver: PropertiesResolver, tokenScope: GitRemote.TokenScope): Github

}