package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 12 Sep 2016
 */
interface Dependencies<P> : ScriptBlock<P> {
    fun dependency(scope: String, dependency: String): Any
    fun dependencies(scope: String, vararg dependencies: String): Dependencies<P>
    fun compile(vararg compileDependencies: String): Dependencies<P>
    fun runtime(vararg runtimeDependencies: String): Dependencies<P>
    fun testCompile(vararg testCompileDependencies: String): Dependencies<P>
    fun integrationTestCompile(vararg integrationTestCompileDependencies: String): Dependencies<P>
}