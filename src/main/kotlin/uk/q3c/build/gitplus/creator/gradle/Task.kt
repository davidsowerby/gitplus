package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 12 Sep 2016
 */
interface Task : ScriptBlock<GradleFileContent> {
    fun dependsOn(dependsOn: String): Task
    fun type(type: String): Task
    fun attribute(attributeName: String, attributeValue: String): Task
}