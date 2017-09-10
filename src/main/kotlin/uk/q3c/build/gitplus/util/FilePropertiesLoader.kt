package uk.q3c.build.gitplus.util

import uk.q3c.build.gitplus.BINTRAY_TOKEN
import uk.q3c.build.gitplus.notSpecified
import uk.q3c.build.gitplus.remote.ServiceProvider
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Loads properties from a File source, which by default is user/home/gitplus/gitplus.properties, but can be set by [source]
 *
 *
 * Created by David Sowerby on 11 Mar 2016
 */
class FilePropertiesLoader : PropertiesLoader {
    override fun getPropertyValue(property: GitPlusProperty, serviceProvider: ServiceProvider): String {
        val propertyName = propertyLookup(property, serviceProvider)
        return retrieveValue(propertyName)
    }


    var source = File(File(System.getProperty("user.home")), "gitplus/gitplus.properties")


    override var properties: Properties = Properties()

    fun sourceFromGradle(): PropertiesLoader {
        source = File(File(System.getProperty("user.home")), "gradle/gradle.properties")
        return this
    }

    fun sourceFromGitPlus(): PropertiesLoader {
        source = File(File(System.getProperty("user.home")), "gitplus/gitplus.properties")
        return this
    }

    fun source(source: File): FilePropertiesLoader {
        this.source = source
        return this
    }

    override fun source(sourcePath: String): PropertiesLoader {
        return source(File(sourcePath))
    }


    fun load(): Boolean {
        try {
            FileInputStream(source).use { fis -> properties.load(fis) }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun bintrayToken(): String {
        return properties[BINTRAY_TOKEN] as String
    }


    private fun retrieveValue(key: String): String {
        if (load()) {
            val value = properties[key]
            if (value == null) {
                return notSpecified
            } else {
                return value as String
            }
        }
        return notSpecified
    }
}
