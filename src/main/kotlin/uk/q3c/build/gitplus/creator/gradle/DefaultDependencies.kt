package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.compile
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.integrationTestCompile
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.runtime
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.testCompile

/**
 * Created by David Sowerby on 12 Sep 2016
 */
class DefaultDependencies<P>(block: ScriptBlock<P>) : Dependencies<P>, ScriptBlock<P> by block {

    init {
        block.setOwner(this)
    }

    override fun dependency(scope: String, dependency: String): Dependency {
        return addDependency(scope, dependency)
    }


    override fun dependencies(scope: String, vararg dependencies: String): Dependencies<P> {
        for (compileDependency in dependencies) {
            addDependency(scope, compileDependency)
        }
        return this
    }


    override fun compile(vararg compileDependencies: String): Dependencies<P> {
        return dependencies(compile, *compileDependencies)
    }


    override fun runtime(vararg runtimeDependencies: String): Dependencies<P> {
        return dependencies(runtime, *runtimeDependencies)
    }


    override fun testCompile(vararg testCompileDependencies: String): Dependencies<P> {
        return dependencies(testCompile, *testCompileDependencies)
    }


    override fun integrationTestCompile(vararg integrationTestCompileDependencies: String): Dependencies<P> {
        return dependencies(integrationTestCompile, *integrationTestCompileDependencies)
    }

    private fun addDependency(scope: String, dependency: String): Dependency {
        val dependency1 = DefaultDependency(scope, dependency, DefaultScriptBlock<Dependencies<*>>(this, dependency))
        return addElement(dependency1)
    }
}