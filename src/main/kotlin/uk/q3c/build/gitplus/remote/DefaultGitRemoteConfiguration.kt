package uk.q3c.build.gitplus.remote

import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.collect.ImmutableMap
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.local.GitLocalConfiguration
import uk.q3c.build.gitplus.notSpecified

/**
 * Created by David Sowerby on 16 Oct 2016
 */
data class DefaultGitRemoteConfiguration(
        override var repoUser: String = notSpecified,
        override var repoName: String = notSpecified)

    : GitRemoteConfiguration {

    override var version = 1
    override var active = true
    override var confirmDelete: String = ""
    @JsonIgnore
    override var repoDeleteApprover: RemoteRepoDeleteApprover = DefaultRemoteRepoDeleteApprover()

    override var projectDescription = ""
    override var projectHomePage = ""
    override var publicProject: Boolean = false

    override var create: Boolean = false
    override var issueLabels: Map<String, String> = defaultIssueLabels
    override var mergeIssueLabels = false
    override var providerBaseUrl: String = "github.com"


    override fun deleteRepoApproved(): Boolean {
        return repoDeleteApprover.isApproved(this)
    }


    /**
     * The issueLabels field is set to an immutable copy of [issueLabels]
     *
     * @param issueLabels the issues to use
     *
     * @return this for fluency
     */
    override fun issueLabels(issueLabels: Map<String, String>): GitRemoteConfiguration {
        this.issueLabels = ImmutableMap.copyOf(issueLabels)
        return this
    }

    override fun mergeIssueLabels(mergeIssueLabels: Boolean): GitRemoteConfiguration {
        this.mergeIssueLabels = mergeIssueLabels
        return this
    }

    override fun active(value: Boolean): GitRemoteConfiguration {
        this.active = value
        return this
    }

    override fun create(value: Boolean): GitRemoteConfiguration {
        this.create = value
        return this
    }

    override fun repoName(remoteRepoName: String): GitRemoteConfiguration {
        this.repoName = remoteRepoName
        return this
    }

    override fun repoUser(remoteRepoUser: String): GitRemoteConfiguration {
        this.repoUser = remoteRepoUser
        return this
    }

    override fun projectDescription(projectDescription: String): GitRemoteConfiguration {
        this.projectDescription = projectDescription
        return this
    }

    override fun projectHomePage(projectHomePage: String): GitRemoteConfiguration {
        this.projectHomePage = projectHomePage
        return this
    }

    override fun publicProject(publicProject: Boolean): GitRemoteConfiguration {
        this.publicProject = publicProject
        return this
    }

    override fun confirmDelete(confirmRemoteDelete: String): GitRemoteConfiguration {
        this.confirmDelete = confirmRemoteDelete
        return this
    }


    override fun remoteRepoFullName(): String {
        return "$repoUser/$repoName"
    }

    override fun repoDeleteApprover(repoDeleteApprover: RemoteRepoDeleteApprover): GitRemoteConfiguration {
        this.repoDeleteApprover = repoDeleteApprover
        return this
    }

    override fun setupFromOrigin(origin: String) {
        val remainder = origin.removePrefix("https://").removeSuffix(".git")
        val segments = remainder.split("/")
        providerBaseUrl = segments[0]
        repoUser = segments[1]
        repoName = segments[2]
    }

    override fun validate(local: GitLocalConfiguration) {
        if (repoName == notSpecified) {
            repoName(local.projectName)
        }

        if (repoName == notSpecified) {
            throw GitPlusConfigurationException("'repoName' must be specified, either directly or through local.projectName()")
        }
        if (repoUser == notSpecified) {
            throw GitPlusConfigurationException("'repoUser' must be specified")
        }
    }


    override fun copyFrom(other: GitRemoteConfiguration) {
        this.projectDescription = other.projectDescription
        this.projectHomePage = other.projectHomePage
        this.publicProject = other.publicProject
        this.repoUser = other.repoUser
        this.repoName = other.repoName
        this.issueLabels = ImmutableMap.copyOf(other.issueLabels)
        this.mergeIssueLabels = other.mergeIssueLabels
        this.confirmDelete = other.confirmDelete
        this.create = other.create
        this.repoDeleteApprover = other.repoDeleteApprover
        this.providerBaseUrl = other.providerBaseUrl
        this.active = other.active
        this.version = other.version
    }


    companion object {

        val defaultIssueLabels: Map<String, String> = ImmutableMap.Builder<String, String>().put("bug", "fc2929").put("duplicate", "cccccc").put("enhancement", "84b6eb").put("question", "cc317c").put("wontfix", "d7e102").put("task", "0b02e1").put("quality", "02d7e1").put("documentation", "eb6420").put("build", "fbca04").put("performance", "d4c5f9").put("critical", "e11d21").build()
    }


}

