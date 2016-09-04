package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.eclipse
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.groovy
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.idea
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.java
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.maven
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.mavenPublish

/**
 * Created by David Sowerby on 12 Sep 2016
 */
class DefaultPlugins(block: ScriptBlock<GradleFileContent>) : Plugins, ScriptBlock<GradleFileContent> by block {

    init {
        block.setOwner(this)
    }

    override fun idea(): Plugins {
        createLine(idea)
        return this
    }

    override fun eclipse(): Plugins {
        createLine(eclipse)
        return this
    }

    override fun plugins(vararg pluginIds: String): Plugins {
        lines("id", true, *pluginIds)
        return this
    }

    override fun java(): Plugins {
        createLine(java)
        return this
    }

    override fun groovy(): Plugins {
        createLine(groovy)
        return this
    }

    override fun maven(): Plugins {
        createLine(maven)
        return this
    }

    override fun mavenPublish(): Plugins {
        createLine(mavenPublish)
        return this
    }

    fun createLine(content: String) {
        lines("id", true, content)
    }


}