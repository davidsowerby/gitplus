package uk.q3c.build.gitplus.local

/**
 * An incredibly simple representation of a Git branch, not dependent on specific implementations
 *
 * Created by David Sowerby on 31 Oct 2016
 */
data class GitBranch(val name: String) {

    fun ref(): String {
        return "refs/heads/$name"
    }

    override fun toString(): String {
        return name
    }
}

fun developBranch(): GitBranch {
    return GitBranch("develop")
}