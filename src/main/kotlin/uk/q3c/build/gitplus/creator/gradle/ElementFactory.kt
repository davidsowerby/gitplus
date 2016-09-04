package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.buildScript
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.dependencies
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.fileContent
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.plugins
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.repositories
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.task
import java.io.File


/**
 * Created by David Sowerby on 14 Sep 2016
 */
object ElementFactory {


    fun <P> repositories(parentElement: P): Repositories<P> {
        return DefaultRepositories<P>(DefaultScriptBlock<P>(parentElement, repositories))
    }


    fun <P> dependencies(parentElement: P): Dependencies<P> {
        return DefaultDependencies<P>(DefaultScriptBlock<P>(parentElement, dependencies))
    }

    fun plugins(parentElement: GradleFileContent): Plugins {
        return DefaultPlugins(DefaultScriptBlock(parentElement, plugins))
    }

    fun buildscript(parentElement: GradleFileContent): BuildScript {
        return DefaultBuildScript(DefaultScriptBlock(parentElement, buildScript))
    }

    fun config(parentElement: GradleFileContent, elementName: String): Config {
        return DefaultConfig(DefaultScriptBlock(parentElement, elementName))
    }

    fun task(parentElement: GradleFileContent, taskName: String): Task {
        return DefaultTask(DefaultScriptBlock(parentElement, task), taskName)
    }

    fun fileContent(gradleFile: GradleFile): GradleFileContent {
        return DefaultGradleFileContent(DefaultScriptBlock<GradleFile>(gradleFile, fileContent))
    }

    fun gradleFile(projectDir: File): GradleFile {
        return DefaultGradleFile(projectDir)
    }

}