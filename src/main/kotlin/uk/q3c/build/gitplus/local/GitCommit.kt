package uk.q3c.build.gitplus.local

import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.revwalk.RevCommit
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

/**
 * Captures commit information.  Primary constructor is mainly for testing.  General use is to pass a [RevCommit] instance - note that
 * [RevWalk.parseBody] must be called on [revCommit] before passing to the constructor
 *
 * Created by David Sowerby on 08 Mar 2016
 */
class GitCommit(val fullMessage: String,
                val hash: String,
                val author: PersonIdent,
                val committer: PersonIdent) {


    val commitDate: ZonedDateTime
    val authorDate: ZonedDateTime
    val shortMessage: String

    /**
     * [RevWalk.parseBody] must be called on [revCommit] before passing to this constructor
     */
    constructor(revCommit: RevCommit) : this(
            revCommit.fullMessage,
            revCommit.id.name,
            revCommit.authorIdent,
            revCommit.committerIdent)


    private val log = LoggerFactory.getLogger(this.javaClass.name)

    init {
        authorDate = identToDate(author)
        commitDate = identToDate(committer)
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
