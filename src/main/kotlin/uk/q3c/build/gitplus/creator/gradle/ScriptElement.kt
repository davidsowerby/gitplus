package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 11 Sep 2016
 */
interface ScriptElement {

    fun writeToBuffer()

    fun getName(): String
}