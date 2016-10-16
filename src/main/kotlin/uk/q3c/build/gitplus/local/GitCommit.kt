package uk.q3c.build.gitplus.local

import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.revwalk.RevCommit
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.remote.GPIssue
import java.time.ZonedDateTime

/**
 * Captures commit information.  Fixes are extracted from commit comments and stored as instances of [GPIssue] if other providers (BitBucket etc) are
 * implemented, their issues would need to be mapped to [GPIssue]
 *
 * Make sure you have called RevWalk.parseBody on [revCommit] before passing to this constructor
 *
 * Created by David Sowerby on 08 Mar 2016
 */
class GitCommit(revCommit: RevCommit) {
    private val log = LoggerFactory.getLogger(this.javaClass.name)


    val fullMessage: String
    val shortMessage: String
    val hash: String
    val author: PersonIdent
    val committer: PersonIdent
    val commitDate: ZonedDateTime
    val authorDate: ZonedDateTime

    init {

        hash = revCommit.id.name
        committer = revCommit.committerIdent
        author = revCommit.authorIdent
        authorDate = identToDate(author)
        commitDate = identToDate(committer)
        fullMessage = revCommit.fullMessage
        shortMessage = extractShortMessage()
    }


    private fun extractShortMessage(): String {
        return fullMessage.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val gitCommit = other as GitCommit

        return hash == gitCommit.hash

    }

    override fun hashCode(): Int {
        return hash.hashCode()
    }


    private fun identToDate(personIdent: PersonIdent): ZonedDateTime {
        val `when` = personIdent.`when`
        return `when`.toInstant().atZone(personIdent.timeZone.toZoneId())
    }


}
