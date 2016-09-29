package uk.q3c.build.gitplus.creator.gradle

import java.io.File

/**
 * Created by David Sowerby on 30 Sep 2016
 */
interface GradleBuilder {
    fun mavenPublishing(): GradleGroovy
    fun writeToFile(file: File)
    fun plugins(vararg pluginIds: String): GradleGroovy
    fun config(name: String): Config
    fun kotlin(version: String): GradleGroovy
    fun testSets(vararg testSetNames: String): GradleGroovy
    fun applyPlugin(name: String): GradleGroovy
    fun applyFrom(name: String): GradleGroovy
    fun wrapper(gradleVersion: String)
    fun task(name: String, type: String = "", dependsOn: String = "", plugin: String = "")
    fun spock(scope: String = "testCompile", version: String = "1.0-groovy-2.4")
    fun dependencies(scope: String = "compile"): Dependencies
    fun java(sourceLevel: String)
    fun baseVersion(version: String = "0.0.1")
    fun dependencies(): Dependencies
    fun repositories(): Repositories
    fun buildscript(): Buildscript
}