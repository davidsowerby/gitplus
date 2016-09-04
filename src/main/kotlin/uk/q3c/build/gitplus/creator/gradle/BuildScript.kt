package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 12 Sep 2016
 */
interface BuildScript : ScriptElement {
    fun repositories(vararg repositoryNames: String): Repositories<BuildScript>
    fun dependencies(): Dependencies<BuildScript>

}