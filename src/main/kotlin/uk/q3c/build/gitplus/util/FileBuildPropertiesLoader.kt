package uk.q3c.build.gitplus.util

import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.remote.ServiceProvider
import uk.q3c.build.gitplus.remote.UnsupportedServiceProviderException
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Loads properties from a File source, which by default is user/home/.gradle/gradle.properties, but can be set
 *
 *
 * Created by David Sowerby on 11 Mar 2016
 */
class FileBuildPropertiesLoader : BuildPropertiesLoader {
    var source = File(File(System.getProperty("user.home")), ".gradle/gradle.properties")
    override var properties: Properties = Properties()

    fun source(source: File): FileBuildPropertiesLoader {
        this.source = source
        return this
    }

    override fun load(): BuildPropertiesLoader {
        try {
            FileInputStream(source).use { fis -> properties.load(fis) }
        } catch (e: Exception) {
            throw GitPlusConfigurationException("Unable to load build properties", e)
        }
        return this
    }

    override fun bintrayToken(): String {
        return properties[BuildPropertiesLoader.BINTRAY_TOKEN] as String
    }

    override fun apiTokenRestricted(serviceProvider: ServiceProvider): String {
        if (serviceProvider == ServiceProvider.GITHUB) {
            return retrieveValue(BuildPropertiesLoader.GITHUB_TOKEN_RESTRICTED)
        } else {
            throw UnsupportedServiceProviderException(serviceProvider)
        }
    }

    override fun apiTokenRepoCreate(serviceProvider: ServiceProvider): String {
        if (serviceProvider == ServiceProvider.GITHUB) {
            return retrieveValue(BuildPropertiesLoader.GITHUB_TOKEN_CREATE_REPO)
        } else {
            throw UnsupportedServiceProviderException(serviceProvider)
        }
    }

    override fun apiTokenRepoDelete(serviceProvider: ServiceProvider): String {
        if (serviceProvider == ServiceProvider.GITHUB) {
            return retrieveValue(BuildPropertiesLoader.GITHUB_TOKEN_DELETE_REPO)
        } else {
            throw UnsupportedServiceProviderException(serviceProvider)
        }
    }

    override fun taggerEmail(): String {
        return retrieveValue(BuildPropertiesLoader.TAGGER_EMAIL)
    }

    override fun taggerName(): String {
        return retrieveValue(BuildPropertiesLoader.TAGGER_NAME)
    }

    private fun retrieveValue(key: String): String {
        load()
        val value = properties[key] ?: throw MissingPropertyException(key)
        return value as String
    }
}
