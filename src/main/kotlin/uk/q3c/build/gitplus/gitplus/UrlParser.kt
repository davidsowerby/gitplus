package uk.q3c.build.gitplus.gitplus

import com.google.common.base.Splitter
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.notSpecified
import uk.q3c.build.gitplus.remote.ServiceProvider
import uk.q3c.build.gitplus.remote.ServiceProvider.BITBUCKET
import uk.q3c.build.gitplus.remote.ServiceProvider.GITHUB
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by David Sowerby on 21 Sep 2017
 */
interface UrlParser {
    /**
     * Decomposes / parses [url] and returns a [RepoDescriptor]
     *
     * @throws MalformedURLException if the URL is malformed
     * @throws RepoException if the url is not recognised as a repo
     */
    fun repoDescriptor(url: String): RepoDescriptor

    fun repoDescriptor(url: URL): RepoDescriptor

    /**
     * Decomposes / parses [url] and returns an [IssueDescriptor]
     *
     * @throws MalformedURLException if the URL is malformed
     * @throws RepoException if the url is not recognised as a repo
     */
    fun issueDescriptor(url: String): IssueDescriptor

    fun issueDescriptor(url: URL): IssueDescriptor
}

class DefaultUrlParser : UrlParser {
    private val log = LoggerFactory.getLogger(this.javaClass.name)

    override fun repoDescriptor(url: String): RepoDescriptor {
        return repoDescriptor(URL(url))
    }

    override fun repoDescriptor(url: URL): RepoDescriptor {
        return descriptor(url, false) as RepoDescriptor
    }

    private fun descriptor(url: URL, issue: Boolean): Descriptor {
        log.debug("parsing ${url.toExternalForm()}")
        var issues = notSpecified
        var issueNumber = -1
        var repoDescriptor: RepoDescriptor

        try {
            val host = url.host
            val segments = Splitter.on("/").split(url.path).toList()
            val repoUser = segments[1]
            val repoName = segments[2]
            val provider =
                    if (host.contains("bitbucket")) {
                        BITBUCKET
                    } else {
                        GITHUB
                    }



            if (segments.size >= 5) {
                log.debug("checking additional segment(s) for issue identification")
                issues = segments[3]
                issueNumber = segments[4].toInt()
            }


            repoDescriptor = RepoDescriptor(host = host, repoUser = repoUser, repoName = repoName, provider = provider)

            if (!issue) {
                return repoDescriptor
            }
        } catch (e: Exception) {
            throw RepoException("${url.toExternalForm()} is not a valid repository URL", e)
        }


        if (issues == "issues" && issueNumber > 0) {
            return IssueDescriptor(repoDescriptor, issueNumber)
        }
        throw RepoException("${url.toExternalForm()} is not a recognised as an issue URL")

    }


    override fun issueDescriptor(url: String): IssueDescriptor {
        return issueDescriptor(URL(url))
    }

    override fun issueDescriptor(url: URL): IssueDescriptor {
        return descriptor(url, true) as IssueDescriptor
    }

}

class RepoException(msg: String, e: Exception?) : RuntimeException(msg, e) {
    constructor(msg: String) : this(msg, null)
}


interface Descriptor {
    fun toUrl(): String
}

data class RepoDescriptor @JvmOverloads constructor(val host: String, val repoUser: String, val repoName: String, val provider: ServiceProvider = GITHUB) : Descriptor {
    override fun toUrl(): String {
        return "$host/$repoUser/$repoName"
    }
}

data class IssueDescriptor(val repoDescriptor: RepoDescriptor, val issueNumber: Int) : Descriptor {
    override fun toUrl(): String {
        return "${repoDescriptor.toUrl()}/issues/$issueNumber"
    }
}