package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.buildScript
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.dependencies
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.plugins
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.repositories
import java.io.File

/**
 * Created by David Sowerby on 11 Sep 2016
 */
class DefaultGradleFileContent(block: ScriptBlock<GradleFile>) : GradleFileContent, ScriptBlock<GradleFile> by block {

    var fileBuffer: FileBuffer

    init {
        fileBuffer = DefaultFileBuffer
        block.setHolderOnly(true)
        block.setOwner(this)
    }

    override fun config(elementName: String): Config {
        return addElement(ElementFactory.config(this, elementName))
    }

    override fun repositories(vararg repositoryNames: String): Repositories<GradleFileContent> {
        @Suppress("UNCHECKED_CAST")
        val repositories1: Repositories<GradleFileContent> =
                if (contains(repositories)) {
                    getInstanceOf(repositories) as Repositories<GradleFileContent>
                } else {
                    addElement(ElementFactory.repositories(this)) as Repositories<GradleFileContent>
                }
        repositories1.repositories(*repositoryNames)
        return repositories1
    }


    override fun plugins(vararg pluginNames: String): Plugins {
        val plugins1: Plugins =
                if (contains(plugins)) {
                    getInstanceOf(plugins) as Plugins
                } else {
                    addElement(ElementFactory.plugins(this))
                }
        plugins1.plugins(*pluginNames)
        return plugins1
    }


    override fun writeToFile(outputFile: File) {
        writeToBuffer()
        fileBuffer.writeToFile(outputFile)
    }


    override fun sourceCompatibility(level: String): GradleFileContent {
        lines("sourceCompatibility = $level")
        return this
    }

    override fun dependencies(): Dependencies<GradleFileContent> {
        @Suppress("UNCHECKED_CAST")
        val instance: Dependencies<GradleFileContent> =
                if (contains(dependencies)) {
                    getInstanceOf(dependencies) as Dependencies<GradleFileContent>
                } else {
                    addElement(ElementFactory.dependencies(this)) as Dependencies<GradleFileContent>
                }
        return instance
    }

    override fun buildscript(): BuildScript {
        val buildScript1: BuildScript =
                if (contains(buildScript)) {
                    getInstanceOf(plugins) as BuildScript
                } else {
                    addElement(ElementFactory.buildscript(this))
                }
        return buildScript1
    }

    override fun apply(plugin: String): GradleFileContent {
        lines("apply plugin: '$plugin'")
        return this
    }

    override fun wrapper(version: String): GradleFileContent {
        val task = task("wrapper")
        task.type("Wrapper").lines("gradleVersion = '$version'")
        return this
    }

    override fun task(taskName: String): Task {
        return addElement(ElementFactory.task(this, taskName))
    }


    override fun applyFrom(pluginSource: String): GradleFileContent {
        lines("apply from: '$pluginSource'")
        return this
    }

    override fun junit(scope: String, version: String): GradleFileContent {
        dependencies().dependency(scope, "junit:junit:" + version)
        return this
    }

    override fun spock(scope: String, version: String): GradleFileContent {
        dependencies().dependency(scope, "org.spockframework:spock-core:" + version)
        dependencies().dependency(scope, "cglib:cglib-nodep:3.2.0") // needed for Spock mocking
        dependencies().dependency(scope, "org.objenesis:objenesis:2.2") // needed for Spock mocking
        return this
    }

    override fun groovy(scope: String, version: String): GradleFileContent {
        dependencies().dependency(scope, "org.codehaus.groovy:groovy-all:" + version)
        return this
    }

    override fun kotlin(scope: String, version: String): GradleFileContent {
        dependencies().dependency(scope, "org.jetbrains.kotlin:kotlin-stdlib:" + version)
        dependencies().dependency(scope, "org.jetbrains.kotlin:kotlin-reflect:" + version)
        return this
    }

    override fun publishing(enabled: Boolean): GradleFileContent {
        return this
    }

}

fun quoted(line: String): String {
    return "'$line'"
}

fun doubleQuoted(line: String): String {
    return '\"' + line + '\"'
}


