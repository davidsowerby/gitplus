package uk.q3c.build.gitplus.local

import uk.q3c.build.gitplus.gitplus.FileDeleteApprover
import java.io.File

/**
 * Created by David Sowerby on 17 Oct 2016
 */
interface GitLocalConfiguration {

    /**
     * If true, the local Git instance is considered active.  Switch to false if you only want to work with the remote
     * instance
     */
    var active: Boolean
    /**
     * The directory within which a project directory is to be created.  For example, 'user/home/git' where the project directory
     * will become 'user/home/git/myproject'
     */
    var projectDirParent: File
    var projectName: String
    /**
     * Determines the response to a situation where a clone is requested, but a local copy already exists
     */
    var cloneExistsResponse: CloneExistsResponse
    var fileDeleteApprover: FileDeleteApprover
    var taggerName: String
    var taggerEmail: String
    /**
     * If true, create a local copy.
     */
    var create: Boolean
    /**
     * If true, create a local copy by cloning.
     */
    var cloneFromRemote: Boolean

    fun cloneExistsResponse(cloneExistsResponse: CloneExistsResponse): GitLocalConfiguration
    fun fileDeleteApprover(fileDeleteApprover: FileDeleteApprover): GitLocalConfiguration
    fun projectName(projectName: String): GitLocalConfiguration
    fun projectDirParent(projectDirParent: File): GitLocalConfiguration
    fun taggerEmail(taggerEmail: String): GitLocalConfiguration
    fun taggerName(taggerName: String): GitLocalConfiguration
    fun cloneFromRemote(value: Boolean): GitLocalConfiguration
    fun create(value: Boolean): GitLocalConfiguration
    fun active(value: Boolean): GitLocalConfiguration



    /**
     * returns the project directory for the local repo, form from the [projectDirParent] and [projectName]
     */
    fun projectDir(): File

    fun validate()
}

enum class CloneExistsResponse {
    DELETE, PULL, EXCEPTION
}



