package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.gradle.element.PluginElement

class Plugins : NamedBlock() {

    override fun blockName(): String {
        return "plugins"
    }

    override operator fun String.unaryPlus() {
        elements.add(PluginElement(this))
    }
}