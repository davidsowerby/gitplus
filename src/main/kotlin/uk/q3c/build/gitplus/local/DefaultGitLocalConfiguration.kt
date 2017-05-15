package uk.q3c.build.gitplus.local

import com.fasterxml.jackson.annotation.JsonIgnore
import uk.q3c.build.gitplus.gitplus.DefaultFileDeleteApprover
import uk.q3c.build.gitplus.gitplus.FileDeleteApprover
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.local.CloneExistsResponse.EXCEPTION
import uk.q3c.build.gitplus.notSpecified
import uk.q3c.build.gitplus.remote.GitRemote
import java.io.File

/**
 * Created by David Sowerby on 17 Oct 2016
 */
data class DefaultGitLocalConfiguration(override var projectName: String = notSpecified) : GitLocalConfiguration {
    override var active = true
    override var cloneExistsResponse = EXCEPTION
    @JsonIgnore
    override var fileDeleteApprover: FileDeleteApprover = DefaultFileDeleteApprover()

    override var projectDirParent: File = File(".")
    override var taggerName: String = notSpecified
    override var taggerEmail: String = notSpecified
    override var create = false
    override var cloneFromRemote = false
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

    override fun projectCreator(projectCreator: ProjectCreator): GitLocalConfiguration {
        this.projectCreator = projectCreator
        return this
    }

    override fun validate(remote: GitRemote) {

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
}