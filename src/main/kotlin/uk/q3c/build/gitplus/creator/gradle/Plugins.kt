package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 12 Sep 2016
 */
interface Plugins : ScriptBlock<GradleFileContent> {
    fun java(): Plugins
    fun groovy(): Plugins
    fun maven(): Plugins
    fun mavenPublish(): Plugins
    fun idea(): Plugins
    fun eclipse(): Plugins
    fun plugins(vararg pluginIds: String): Plugins
    /**
     * Add a plugin with a specific version, for example:  `id 'org.unbroken-dome.test-sets' version '1.2.0'`
     */
    fun plugin(pluginId: String, version: String): Plugins
}