package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 11 Sep 2016
 */
class DefaultRepositories<P>(block: ScriptBlock<P>) : Repositories<P>, ScriptBlock<P> by block {

    init {
        block.setOwner(this)
    }

    override fun repositories(vararg repositories: String): Repositories<P> {
        for (repository in repositories) {
            lines(repository)
        }
        return this
    }

    override fun mavenLocal(): DefaultRepositories<P> {
        lines("mavenLocal()")
        return this
    }

    override fun mavenCentral(): DefaultRepositories<P> {
        lines("mavenCentral()")
        return this
    }


    override fun jcenter(): DefaultRepositories<P> {
        lines("jcenter()")
        return this
    }
}