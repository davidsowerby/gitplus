package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.space

/**
 * Created by David Sowerby on 12 Sep 2016
 */
class DefaultDependency(val scope: String, val dependencyStr: String, val block: ScriptBlock<Dependencies<*>>) : Dependency, ScriptBlock<Dependencies<*>> by block {

    init {
        block.setBlockHeading { name -> heading() }
        block.setAsLine { name -> asLine() }
        block.setOwner(this)
    }

    private fun heading(): String {

        val s = quotedDependencyStr()
        return "$scope$space($s)"
    }

    private fun asLine(): String {
        val s = quotedDependencyStr()
        return "$scope$space$s"
    }

    override fun excludeModule(exclusion: String): Dependency {
        lines("exclude module:", true, exclusion)
        return this
    }

    /**
     * if it is a built-in (eg gradleApi()) do not quote it
     */
    private fun quotedDependencyStr(): String {
        val quotedDependencyStr = if (dependencyStr.contains("()")) dependencyStr else quoted(dependencyStr)
        return quotedDependencyStr
    }

}