package uk.q3c.build.gitplus.local

import uk.q3c.build.gitplus.gitplus.FileDeleteApprover
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.local.CloneExistsResponse.EXCEPTION
import uk.q3c.build.gitplus.notSpecified
import java.io.File

/**
 * Created by David Sowerby on 17 Oct 2016
 */
open class DefaultGitLocalConfiguration : GitLocalConfiguration {
    override var active = true
    override var cloneExistsResponse = EXCEPTION
    override lateinit var projectName: String
    override lateinit var fileDeleteApprover: FileDeleteApprover
    override var projectDirParent: File = File(".")
    override var taggerName: String = notSpecified
    override var taggerEmail: String = notSpecified
    override var create = false
    override var cloneFromRemote = false

    override fun cloneFromRemote(value: Boolean): GitLocalConfiguration {
        this.cloneFromRemote = value
        return this
    }

    override fun active(value: Boolean): GitLocalConfiguration {
        this.active = value
        return this
    }

    override fun create(value: Boolean): GitLocalConfiguration {
        this.create = value
        return this
    }

    override fun projectName(projectName: String): GitLocalConfiguration {
        this.projectName = projectName
        return this
    }


    override fun projectDirParent(projectDirParent: File): GitLocalConfiguration {
        this.projectDirParent = projectDirParent
        return this
    }

    override fun taggerEmail(taggerEmail: String): GitLocalConfiguration {
        this.taggerEmail = taggerEmail
        return this
    }

    override fun taggerName(taggerName: String): GitLocalConfiguration {
        this.taggerName = taggerName
        return this
    }

    override fun cloneExistsResponse(cloneExistsResponse: CloneExistsResponse): GitLocalConfiguration {
        this.cloneExistsResponse = cloneExistsResponse
        return this
    }

    override fun fileDeleteApprover(fileDeleteApprover: FileDeleteApprover): GitLocalConfiguration {
        this.fileDeleteApprover = fileDeleteApprover
        return this
    }

    override fun projectDir(): File {
        return File(projectDirParent, projectName)
    }

    override fun validate() {
        if (create && cloneFromRemote) {
            throw GitPlusConfigurationException("Local repo cannot be both created and cloned")
        }
    }
}