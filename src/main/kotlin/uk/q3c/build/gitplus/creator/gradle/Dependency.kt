package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.gradle.element.BasicScriptElement
import uk.q3c.build.gitplus.creator.gradle.element.ScriptElement

class Dependency(val scope: String, dependency: String) : BasicScriptElement(dependency) {

    val elements: MutableSet<ScriptElement> = mutableSetOf()

    fun excludeGroup(group: String): Dependency {
        elements.add(BasicScriptElement("exclude group: '$group'"))
        return this
    }

    fun excludeModule(module: String): Dependency {
        elements.add(BasicScriptElement("exclude module: '$module'"))
        return this
    }

    fun excludeGroupModule(group: String, module: String): Dependency {
        elements.add(BasicScriptElement("exclude group: '$group', module: '$module'"))
        return this
    }

    operator fun String.unaryPlus() {
        throw UnsupportedOperationException("'+' cannot be used inside a Dependency")
    }

    override fun write() {
        val quotedContent: String = if (content.contains("()")) content else "'$content'"
        fileBuffer.appendLine(scope, "(", quotedContent, ") {")
        fileBuffer.incrementIndent()
        for (element in elements) {
            element.write()
        }
        fileBuffer.decrementIndent()
        fileBuffer.appendLine("}")
    }


}