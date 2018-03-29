package uk.q3c.build.gitplus.util

import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.notSpecified
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration
import uk.q3c.build.gitplus.remote.ServiceProvider
import uk.q3c.build.gitplus.remote.UnsupportedServiceProviderException
import uk.q3c.build.gitplus.util.GitPlusProperty.*

/**
 * Retrieves external properties (mostly API tokens), typically held in a file or supplied by Gradle for example.
 * Different implementations may be added to [GitRemoteConfiguration.apiPropertiesLoaders]
 *
 *
 * Created by David Sowerby on 03 Sep 2017
 */
interface PropertiesHandler {

    /**
     * Returns a property value, typically an API token, for the [property] and [serviceProvider] given.
     * [PropertiesLoader] implementations return [notSpecified] if they have no value to return
     *
     * @param serviceProvider the remote repository service provider (GitHub, BitBucket etc)
     * @param property the property required
     *
     * @return A property value from the first of the [PropertiesResolver.loaders] which returns a value which is not the same as [notSpecified]
     *
     * @throws NoSuchElementException if no token is found in any [PropertiesLoader]
     */
    fun getPropertyValue(property: GitPlusProperty, serviceProvider: ServiceProvider): String

}

/**
 * Returns the first available property value, by checking each of the [loaders] in turn.  Throws a [NoSuchElementException] when none of the loaders
 * provide a value.  Most of the methods are convenience methods to wrap [getPropertyValue]
 */
interface PropertiesResolver : PropertiesHandler {
    var loaders: MutableList<PropertiesLoader>

    /**
     * Returns an API token with permission to raise issues.  [PropertiesLoader] implementations return [notSpecified] if they have no
     * value to return
     *
     * @param serviceProvider the remote repository service provider (GitHub, BitBucket etc)
     * *
     * @return An API token with permission to raise issues.
     *
     * @throws NoSuchElementException if no token is found in any [PropertiesLoader]
     */
    fun apiTokenIssueCreate(serviceProvider: ServiceProvider): String

    /**
     * Returns an API token with permission to create a repo. [PropertiesLoader] implementations return [notSpecified] if they have
     * no value to return
     *
     * @param serviceProvider the remote repository service provider (GitHub, BitBucket etc)
     * *
     * @return an API token with permission to create a repo
     *
     * @throws NoSuchElementException if no token is found in any [PropertiesLoader]
     */
    fun apiTokenRepoCreate(serviceProvider: ServiceProvider): String

    /**
     * Returns an API token with permission to delete repositories. [PropertiesLoader] implementations return [notSpecified] if they have
     * no value to return
     *
     * @param serviceProvider the remote repository service provider (GitHub, BitBucket etc)
     *
     * @return an API token with permission to delete repositories
     *
     * @throws NoSuchElementException if no token is found in any [PropertiesLoader]
     */
    fun apiTokenRepoDelete(serviceProvider: ServiceProvider): String

    /**
     * Returns the email of the user to associate with a Git tag. [PropertiesLoader] implementations return [notSpecified] if they have
     * no value to return
     *
     * @return the email of the user to associate with a Git tag
     *
     * @throws NoSuchElementException if no value is found in any [PropertiesLoader]
     */
    fun taggerEmail(): String

    /**
     * Returns the name of the user to associate with a Git tag. [PropertiesLoader] implementations return [notSpecified] if they have
     * no value to return
     *
     * @return the name of the user to associate with a Git tag
     *
     * @throws NoSuchElementException if no value is found in any [PropertiesLoader]
     */
    fun taggerName(): String
}

class DefaultPropertiesResolver : PropertiesResolver {
    private val log = LoggerFactory.getLogger(this.javaClass.name)
    override fun getPropertyValue(property: GitPlusProperty, serviceProvider: ServiceProvider): String {

        try {
            val loader = loaders.first { loader -> loader.getPropertyValue(property, serviceProvider) != notSpecified }
            return loader.getPropertyValue(property, serviceProvider)
        } catch (nsee: NoSuchElementException) {
            val msg = "No value found for property ${propertyLookup(property, serviceProvider)}, you either need to add the property so an existing loader can locate it, or add a loader to GitPlusConfiguration.propertiesLoaders"
            log.error(msg)
            throw MissingPropertyException(msg)
        }
    }

    override lateinit var loaders: MutableList<PropertiesLoader>

    override fun taggerEmail(): String {
        // Service provider is not actually used, so can be anything
        return getPropertyValue(TAGGER_EMAIL, ServiceProvider.GITHUB)
    }

    override fun taggerName(): String {
        // Service provider is not actually used, so can be anything
        return getPropertyValue(TAGGER_NAME, ServiceProvider.GITHUB)
    }

    override fun apiTokenRepoDelete(serviceProvider: ServiceProvider): String {
        return getPropertyValue(REPO_DELETE_TOKEN, serviceProvider)
    }

    override fun apiTokenRepoCreate(serviceProvider: ServiceProvider): String {
        return getPropertyValue(REPO_CREATE_TOKEN, serviceProvider)
    }

    override fun apiTokenIssueCreate(serviceProvider: ServiceProvider): String {
        return getPropertyValue(ISSUE_CREATE_TOKEN, serviceProvider)
    }

}

enum class GitPlusProperty {
    ISSUE_CREATE_TOKEN, REPO_CREATE_TOKEN, REPO_DELETE_TOKEN, TAGGER_NAME, TAGGER_EMAIL
}

fun propertyLookup(gitPlusProperty: GitPlusProperty, serviceProvider: ServiceProvider): String {
    if (serviceProvider == ServiceProvider.BITBUCKET) {
        throw UnsupportedServiceProviderException(serviceProvider)
    }

    return when (gitPlusProperty) {
        TAGGER_NAME, TAGGER_EMAIL -> propertyNameFromEnum(gitPlusProperty)
        ISSUE_CREATE_TOKEN, REPO_CREATE_TOKEN, REPO_DELETE_TOKEN -> "${serviceProvider.name.toLowerCase()}-${propertyNameFromEnum(gitPlusProperty)}"
    }

}

fun propertyNameFromEnum(property: GitPlusProperty): String {
    return property.name.toLowerCase().replace("_", "-")
}