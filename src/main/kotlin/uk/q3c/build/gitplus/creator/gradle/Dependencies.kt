package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.gradle.element.BasicScriptElement

class Dependencies : NamedBlock() {

    override fun blockName(): String {
        return "dependencies"
    }

    private var currentScope: String = "compile"

    fun setCurrentScope(scope: String) {
        currentScope = scope
    }

    fun getCurrentScope(): String {
        return currentScope
    }

    /**
     * Adds a string as an element constructed as [currentScope] + the string.  The string is quoted, unless it contains parenthesis
     * (for example in 'gradleApi()'), then the string remains unquoted
     */
    override operator fun String.unaryPlus() {
        if (this.contains("()")) {
            elements.add(BasicScriptElement("$currentScope $this"))
        } else {
            elements.add(BasicScriptElement("$currentScope '$this'"))
        }

    }

    internal fun dependency(scope: String, dependency: String, init: Dependency.() -> Unit): Dependency {
        val dep = Dependency(scope, dependency)
        dep.init()
        elements.add(dep)
        return dep
    }

    fun dependency(scope: String, dependency: String): Dependency {
        val dep = Dependency(scope, dependency)
        elements.add(dep)
        return dep

    }
}