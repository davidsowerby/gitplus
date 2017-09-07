package uk.q3c.build.gitplus.util

import uk.q3c.build.gitplus.remote.GitRemoteConfiguration
import java.util.*

/**
 * Loader for API properties, typically retrieved from a properties file or from Gradle, but implementations may retrieve them from anywhere. Its main purpose it
 * to retrieve API tokens so that they do not need to be coded into configuration files
 *
 * An attempt is made with this interface to restrict the most dangerous access (deleting remote repositories, see
 * [GitRemoteConfiguration.repoDeleteApprover]) - but it is up to the developer to ensure that the necessary protection
 * is in place to avoid accidental deletion of what is often the central repository, and that the tokens provided are generated with
 * the appropriate privileges
 *
 * Created by David Sowerby on 11 Mar 2016
 */
interface PropertiesLoader : PropertiesHandler {

    /**
     * Returns the full properties object to enable access to any properties not explicitly covered by this API.  Call [load] first.
     *
     * @return Returns the full properties object to enable access to any properties not explicitly covered by this API
     */
    val properties: Properties

    /**
     * Returns the API token for Bintray.
     *
     * @return the API token for Bintray.
     */
    @Deprecated("Not relevant to this library, will be removed completely")
    fun bintrayToken(): String


    /**
     * Sets the path to look for properties - usually relates only to File based implementation
     *
     * @return this for fluency
     */
    fun source(sourcePath: String): PropertiesLoader

}
