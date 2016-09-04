package uk.q3c.build.gitplus.creator.gradle

import java.io.File

/**
 * Created by David Sowerby on 11 Sep 2016
 */
interface GradleFileContent : ScriptBlock<GradleFile> {
    fun repositories(vararg repositoryNames: String): Repositories<GradleFileContent>
    fun plugins(vararg pluginNames: String): Plugins
    fun dependencies(): Dependencies<GradleFileContent>
    fun buildscript(): BuildScript
    fun config(elementName: String): Config
    fun apply(plugin: String): GradleFileContent
    fun wrapper(version: String): GradleFileContent
    fun task(taskName: String): Task
    fun applyFrom(pluginSource: String): GradleFileContent
    fun sourceCompatibility(level: String): GradleFileContent
    fun writeToFile(outputFile: File)
    fun junit(scope: String = DefaultScriptBlock.testCompile, version: String = "4.12"): GradleFileContent
    fun spock(scope: String = DefaultScriptBlock.testCompile, version: String = "1.0-groovy-2.4"): GradleFileContent
    fun groovy(scope: String = DefaultScriptBlock.compile, version: String = "2.4.7"): GradleFileContent
    fun kotlin(scope: String = DefaultScriptBlock.compile, version: String = "1.0.4"): GradleFileContent
    fun publishing(enabled: Boolean = true): GradleFileContent
}