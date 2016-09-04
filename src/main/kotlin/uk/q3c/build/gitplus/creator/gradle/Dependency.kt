package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 12 Sep 2016
 */
interface Dependency : ScriptElement {
    fun excludeModule(exclusion: String): Dependency
}