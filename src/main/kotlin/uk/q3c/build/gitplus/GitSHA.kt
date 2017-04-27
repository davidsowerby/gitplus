package uk.q3c.build.gitplus

/**
 * Type safe representation of a Git SHA1 hash.  Validates that [sha] is 40 characters in length
 *
 *
 *
 * Created by David Sowerby on 03 Nov 2016
 */
data class GitSHA(val sha: String) {

    init {
        if (sha.length != 40) {
            throw GitSHAException(sha)
        }
        if (!sha.matches(Regex("-?[0-9a-fA-F]+"))) {
            throw GitSHAException(sha)
        }

    }

    override fun toString(): String {
        return sha
    }

    /**
     * Returns the short version of the hash, namely the first 7 digits of the hash
     */
    fun short(): String {
        return sha.substring(0, 7)
    }
}


class GitSHAException(sha: String) : Throwable("A Git SHA must be in hex and 40 characters in length. '$sha' is ${sha.length} characters")
