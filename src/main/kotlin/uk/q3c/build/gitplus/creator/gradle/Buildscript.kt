package uk.q3c.build.gitplus.creator.gradle

/**
 * Represents the 'buildscript' block in a Gradle grrovy build.gradle
 *
 * The [repositories] and [dependencies] are added to [elements] only when they have been called - this is to stop them
 * being generated as empty blocks if they have not been used.
 */
class Buildscript : NamedBlock() {

    val dependencies = Dependencies()
    val repositories = Repositories()

    override fun blockName(): String {
        return "buildscript"
    }

    fun repositories(init: Repositories.() -> Unit): Repositories {
        repositories.init()
        return repositories()
    }

    fun dependencies(scope: String, init: Dependencies.() -> Unit): Dependencies {
        dependencies.setCurrentScope(scope)
        dependencies.init()
        return dependencies()
    }

    fun dependencies(): Dependencies {
        if (!elements.contains(dependencies)) {
            elements.add(dependencies)
        }
        return dependencies
    }

    fun repositories(): Repositories {
        if (!elements.contains(repositories)) {
            elements.add(repositories)
        }
        return repositories
    }
}