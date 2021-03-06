package uk.q3c.build.gitplus.local

import com.fasterxml.jackson.annotation.JsonIgnore
import uk.q3c.build.gitplus.gitplus.DefaultFileDeleteApprover
import uk.q3c.build.gitplus.gitplus.FileDeleteApprover
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.local.CloneExistsResponse.EXCEPTION
import uk.q3c.build.gitplus.notSpecified
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration
import java.io.File
import java.io.Serializable

/**
 * Created by David Sowerby on 17 Oct 2016
 */
data class DefaultGitLocalConfiguration(override var projectName: String = notSpecified) : GitLocalConfiguration, Serializable {

    override var version = 1
    override var active = true
    override var cloneExistsResponse = EXCEPTION
    @Transient
    @JsonIgnore
    override var fileDeleteApprover: FileDeleteApprover = DefaultFileDeleteApprover()
    @Transient
    @JsonIgnore
    override var projectDirParent: File = File(".")

    override var create = false
    override var cloneFromRemote = false
    @Transient
    @JsonIgnore
    override var projectCreator: ProjectCreator = DefaultProjectCreator()

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

    override fun projectCreator(projectCreator: ProjectCreator): GitLocalConfiguration {
        this.projectCreator = projectCreator
        return this
    }

    override fun validate(remote: GitRemoteConfiguration) {

        if (create && cloneFromRemote) {
            throw GitPlusConfigurationException("Local repo cannot be both created and cloned")
        }

        if (projectName == notSpecified) {
            projectName(remote.repoName)
        }

        if (projectName == notSpecified) {
            throw GitPlusConfigurationException("project name must be set using either local.projectName() or remote.repoName()")
        }
    }

    override fun copyFrom(other: GitLocalConfiguration) {
        this.active = other.active
        this.projectDirParent = other.projectDirParent
        this.projectName = other.projectName
        this.cloneExistsResponse = other.cloneExistsResponse
        this.fileDeleteApprover = other.fileDeleteApprover
        this.create = other.create
        this.cloneFromRemote = other.cloneFromRemote
        this.projectCreator = other.projectCreator
        this.version = other.version
    }
}