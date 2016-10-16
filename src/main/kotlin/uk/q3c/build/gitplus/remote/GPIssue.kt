package uk.q3c.build.gitplus.remote

import com.google.common.base.Preconditions.checkNotNull
import com.jcabi.github.Issue
import com.jcabi.github.IssueLabels
import java.util.*

/**
 * A generic Issue (one that is not specific to a remote provider) - it does not carry everything the 'native' issue does, just what is required for the
 * GitPlus build and changelog functions.
 *
 *
 * Created by David Sowerby on 22 Mar 2016
 */
class GPIssue : Comparable<GPIssue> {

    var title: String = "not specified"
    var labels: MutableSet<String> = HashSet()
    var body: String = "not specified"
    var number: Int = 0
        private set
    var htmlUrl: String = "not specified"
    var isPullRequest: Boolean = false

    constructor(jIssue: Issue) {
        checkNotNull(jIssue)
        val jsIssue = Issue.Smart(jIssue)

        title = jsIssue.title()
        val jLabels = IssueLabels.Smart(jsIssue.labels())
        jLabels.iterate().forEach { l -> this.labels.add(l.name()) }
        body = jsIssue.body()
        number = jsIssue.number()
        htmlUrl = jsIssue.htmlUrl().toExternalForm()
        isPullRequest = jsIssue.isPull
    }

    constructor(number: Int) {
        this.number = number
    }

    fun title(title: String): GPIssue {
        this.title = title
        return this
    }


    fun hasLabel(candidate: String): Boolean {
        return labels.contains(candidate)
    }


    fun labels(labels: MutableSet<String>): GPIssue {
        this.labels = labels
        return this
    }

    fun body(body: String): GPIssue {
        this.body = body
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val gpIssue = other as GPIssue

        if (number != gpIssue.number) {
            return false
        }
        return htmlUrl == gpIssue.htmlUrl

    }

    override fun hashCode(): Int {
        var result = number
        result = 31 * result + htmlUrl.hashCode()
        return result
    }


    fun htmlUrl(htmlUrl: String): GPIssue {
        this.htmlUrl = htmlUrl
        return this
    }

    fun pullRequest(pullRequest: Boolean): GPIssue {
        this.isPullRequest = pullRequest
        return this
    }

    override fun compareTo(other: GPIssue): Int {
        val h = htmlUrl.compareTo(other.htmlUrl)
        if (h != 0) {
            return h
        }
        return this.number - other.number
    }
}
