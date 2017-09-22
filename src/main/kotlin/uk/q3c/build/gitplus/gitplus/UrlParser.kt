package uk.q3c.build.gitplus.gitplus

import com.google.common.base.Splitter
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
     * @throws RepoIssueException if the url is not recognised as an issue
     */
    fun issueDescriptor(url: String): IssueDescriptor

    fun issueDescriptor(url: URL): IssueDescriptor
}

class DefaultUrlParser : UrlParser {

    override fun repoDescriptor(url: String): RepoDescriptor {
        return repoDescriptor(URL(url))
    }

    override fun repoDescriptor(url: URL): RepoDescriptor {
        return descriptor(url, false) as RepoDescriptor
    }

    private fun descriptor(url: URL, issue: Boolean): Descriptor {
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

            var issues = notSpecified
            var issueNumber = -1

            if (segments.size >= 5) {
                issues = segments[3]
                issueNumber = segments[4].toInt()
            }
            val repoDescriptor = RepoDescriptor(host = host, repoUser = repoUser, repoName = repoName, provider = provider)

            if (!issue) {
                return repoDescriptor
            } else {
                if (issues == "issues" && issueNumber > 0) {
                    return IssueDescriptor(repoDescriptor, issueNumber)
                }
                throw RepoIssueException("${url.toExternalForm()} is not a recognised as an issue URL")

            }
        } catch (e: Exception) {
            if (issue) {
                throw RepoIssueException("${url.toExternalForm()} is not a recognised as an issue URL", e)
            } else {
                throw RepoException("${url.toExternalForm()} is not a valid repository URL", e)
            }
        }
    }

    override fun issueDescriptor(url: String): IssueDescriptor {
        return issueDescriptor(URL(url))
    }

    override fun issueDescriptor(url: URL): IssueDescriptor {
        return descriptor(url, true) as IssueDescriptor
    }

}

open class RepoException(msg: String, e: Exception) : RuntimeException(msg, e)

class RepoIssueException(msg: String, e: Exception?) : RuntimeException(msg, e) {
    constructor(msg: String) : this(msg, null)
}

interface Descriptor

data class RepoDescriptor @JvmOverloads constructor(val host: String, val repoUser: String, val repoName: String, val provider: ServiceProvider = GITHUB) : Descriptor

data class IssueDescriptor(val repoDescriptor: RepoDescriptor, val issueNumber: Int) : Descriptor