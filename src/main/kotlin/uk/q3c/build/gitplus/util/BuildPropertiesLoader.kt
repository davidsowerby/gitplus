package uk.q3c.build.gitplus.util

import uk.q3c.build.gitplus.remote.ServiceProvider
import java.io.IOException
import java.util.*

/**
 * Loader for build properties, typically retrieved from gradle.properties, but other implementations may retrieve them from elsewhere. Its main purpose it
 * to provide API tokens.
 *
 * An attempt is made with this interface to restrict the most dangerous access (deleting remote repositories) - but it is up to the developer to ensure that
 * the necessary protection is in place to avoid accidental deletion of what is often the central repository, and that the tokens provided are generated with
 * the appropriate privileges
 *
 * Created by David Sowerby on 11 Mar 2016
 */
interface BuildPropertiesLoader {


    /**
     * Returns the full properties object to enable access to any properties not explicitly covered by this API.  Call [load] first.
     *
     * @return Returns the full properties object to enable access to any properties not explicitly covered by this API
     */
    val properties: Properties

    /**
     * Loads the properties from wherever the implementation decides

     * @return this for fluency
     * *
     * @throws IOException if the properties cannot be loaded, or any required property is missing
     */
    fun load(): BuildPropertiesLoader

    /**
     * Returns the API token for Bintray.
     *
     * @return the API token for Bintray.
     */
    fun bintrayToken(): String

    /**
     * Returns an API token.  It is expected that this token gives limited access rights.  Typically this would be enough
     * to raise issues, but exclude creating / deleting repositories.  It is up to the developer how best to use this and
     * [apiTokenRepoCreate]
     *
     * @param serviceProvider the remote repository service provider (GitHub, BitBucket etc)
     * *
     * @return An API token with restricted privileges
     */
    fun apiTokenRestricted(serviceProvider: ServiceProvider): String

    /**
     * Returns an API token. It is expected that this token gives privileges to create repositories as well as the privileges provided by
     * [apiTokenRestricted]
     *
     * @param serviceProvider the remote repository service provider (GitHub, BitBucket etc)
     * *
     * @return an API token with restricted privileges plus repository create privilege.
     */
    fun apiTokenRepoCreate(serviceProvider: ServiceProvider): String

    /**
     * Returns an API token.  It is expected that this token gives privileges to delete repositories ONLY
     *
     * @param serviceProvider the remote repository service provider (GitHub, BitBucket etc)
     *
     * @return an API token with privileges to delete a repository
     */
    fun apiTokenRepoDelete(serviceProvider: ServiceProvider): String

    /**
     * Returns the email of the user to associate with a Git tag
     *
     * @return the email of the user to associate with a Git tag
     */
    fun taggerEmail(): String

    /**
     * Returns the name of the user to associate with a Git tag

     * @return the name of the user to associate with a Git tag
     */
    fun taggerName(): String

    companion object {
        val BINTRAY_TOKEN = "bintrayToken"
        val TAGGER_NAME = "taggerName"
        val TAGGER_EMAIL = "taggerEmail"
        val GITHUB_TOKEN_RESTRICTED = "githubApiTokenRestricted"
        val GITHUB_TOKEN_CREATE_REPO = "gitHubApiTokenCreateRepo"
        val GITHUB_TOKEN_DELETE_REPO = "gitHubApiTokenDeleteRepo"
    }
}
