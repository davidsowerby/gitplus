package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 11 Sep 2016
 */
interface Repositories<P> : ScriptBlock<P> {
    fun repositories(vararg repositories: String): Repositories<P>
    fun mavenLocal(): Repositories<P>
    fun mavenCentral(): Repositories<P>
    fun jcenter(): Repositories<P>
}