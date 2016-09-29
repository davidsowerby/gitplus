package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.gradle.buffer.DefaultFileBuffer
import uk.q3c.build.gitplus.creator.gradle.buffer.FileBuffer
import uk.q3c.build.gitplus.creator.gradle.element.*
import java.io.File

/**
 * A file builder for Groovy based Gradle script (default name = build.gradle)
 *
 * Created by David Sowerby on 29 Sep 2016
 */
class GradleGroovy : GradleBuilder {


    val mavenLocal = "mavenLocal()"
    val jcenter = "jcenter()"
    val mavenCentral = "mavenCentral()"

    val maven = "maven"
    val mavenPublishing = "maven-publish"

    val buildscript = Buildscript()
    val plugins = Plugins()
    val repositories = Repositories()
    val dependencies = Dependencies()

    val elements: MutableList<ScriptElement> = mutableListOf()

    val fileBuffer: FileBuffer = DefaultFileBuffer

    override fun baseVersion(version: String) {
        elements.add(BaseVersionElement(version))
    }

    fun buildscript(init: Buildscript.() -> Unit): Buildscript {
        buildscript.init()
        return buildscript
    }

    override fun buildscript(): Buildscript {
        return buildscript
    }

    fun repositories(init: Repositories.() -> Unit): Repositories {
        repositories.init()
        return repositories
    }

    override fun repositories(): Repositories {
        return repositories
    }

    fun dependencies(scope: String, init: Dependencies.() -> Unit): Dependencies {
        dependencies.setCurrentScope(scope)
        dependencies.init()
        return dependencies
    }

    override fun dependencies(): Dependencies {
        return dependencies
    }


    override fun dependencies(scope: String): Dependencies {
        dependencies.setCurrentScope(scope)
        return dependencies
    }

    fun plugins(init: Plugins.() -> Unit): Plugins {
        plugins.init()
        return plugins
    }


    fun task(name: String, type: String, dependsOn: String, plugin: String, init: Task.() -> Unit): Task {
        val task = Task(name, type, dependsOn, plugin)
        task.init()
        elements.add(task)
        return task
    }


    fun config(name: String, init: Config.() -> Unit): Config {
        val cfg: Config = Config(name)
        cfg.init()
        elements.add(cfg)
        return cfg
    }

    override fun applyPlugin(name: String): GradleGroovy {
        elements.add(ApplyPluginElement(name))
        return this
    }

    override fun applyFrom(name: String): GradleGroovy {
        elements.add(ApplyFromElement(name))
        return this
    }

    override fun spock(scope: String, version: String) {
        defaultRepositories()
        dependencies(scope) {
            dependency(scope, "org.spockframework:spock-core:$version") {
                excludeModule("groovy-all")
            }
            +"cglib:cglib-nodep:3.2.0"
            +"org.objenesis:objenesis:2.2"
        }
    }


    override fun kotlin(version: String): GradleGroovy {
        buildscript {
            +"ext.kotlin_version = '$version'"
            repositories {
                +jcenter
                +mavenCentral
            }
            dependencies("classpath") {
                +"org.jetbrains.kotlin:kotlin-gradle-plugin:\$kotlin_version"
            }
        }
        defaultRepositories()
        dependencies("compile") {
            +"org.jetbrains.kotlin:kotlin-stdlib:\$kotlin_version"
            +"org.jetbrains.kotlin:kotlin-reflect:\$kotlin_version"
        }
        return this
    }

    private fun defaultRepositories() {
        repositories {
            +jcenter
            +mavenCentral
        }
    }

    override fun config(name: String): Config {
        val cfg: Config = Config(name)
        elements.add(cfg)
        return cfg
    }

    override fun writeToFile(file: File) {
        buildscript.writeBlockToBuffer()
        plugins.writeBlockToBuffer()
        repositories.writeBlockToBuffer()
        dependencies.writeBlockToBuffer()

        for (element in elements) {
            element.write()
        }
        fileBuffer.writeToFile(file)
        fileBuffer.reset()
    }

    override fun java(sourceLevel: String) {
        plugins {
            +"java"
        }
        elements.add(BasicScriptElement("sourceCompatibility = '$sourceLevel'"))
    }

    override fun mavenPublishing(): GradleGroovy {
        plugins {
            +maven
            +mavenPublishing
        }
        config("publishing") {
            config("publications") {
                config("mavenStuff(MavenPublication)") {
                    +"from components.java"
                    config("artifact sourcesJar") {
                        +"classifier 'sources'"
                    }
                    config("artifact javadocJar") {
                        +"classifier 'javadoc'"
                    }
                }
            }

        }
        return this
    }

    override fun task(name: String, type: String, dependsOn: String, plugin: String) {
        elements.add(Task(name = name, type = type, dependsOn = dependsOn, plugin = plugin))
    }

    override fun wrapper(gradleVersion: String) {
        task(name = "wrapper", type = "Wrapper", plugin = "", dependsOn = "") {
            +"gradleVersion = '$gradleVersion'"
        }
    }

    override fun testSets(vararg testSetNames: String): GradleGroovy {
        defaultRepositories()
        config("testSets") {
            for (name in testSetNames) {
                +name
            }
        }
        plugins {
            +"org.unbroken-dome.test-sets version 1.2.0"
        }
        return this
    }

    override fun plugins(vararg pluginIds: String): GradleGroovy {
        for (pluginId in pluginIds) {
            plugins {
                +pluginId
            }
        }
        return this
    }

}


