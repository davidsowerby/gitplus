package uk.q3c.build.gitplus.gitplus

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.local.*
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.GitRemoteResolver
import uk.q3c.build.gitplus.remote.ServiceProvider
import uk.q3c.build.gitplus.util.FilePropertiesLoader
import uk.q3c.build.gitplus.util.PropertiesResolver
import java.io.File
import java.io.StringWriter


class DefaultGitPlus @Inject constructor(
        override val local: GitLocal,
        override val wikiLocal: WikiLocal,
        val remoteResolver: GitRemoteResolver,
        val propertiesResolver: PropertiesResolver,
        override val urlParser: UrlParser,
        override val configuration: GitPlusConfiguration)

    : GitPlus, PropertiesResolver by propertiesResolver, GitPlusConfiguration by configuration {


    private val log = LoggerFactory.getLogger(this.javaClass.name)
    override lateinit var serviceProvider: ServiceProvider
    override lateinit var remote: GitRemote

    init {
        remote = remoteResolver.getDefault()
        serviceProvider = remoteResolver.defaultProvider()
    }

    /**
     * Closes [GitLocal] instances to free up resources
     */
    override fun close() {
        local.close()
        wikiLocal.close()
    }

    override fun useRemoteOnly(remoteRepoUserName: String, projectName: String) {
        local.active = false
        remote.repoUser = remoteRepoUserName
        remote.repoName = projectName
    }

    override fun createRemoteOnly(remoteRepoUserName: String, projectName: String, publicProject: Boolean) {
        local.active = false
        remote.repoUser = remoteRepoUserName
        remote.repoName = projectName
        remote.create = true
        remote.publicProject = publicProject
    }

    override fun createLocalAndRemote(parentDir: File, remoteRepoUserName: String, projectName: String, includeWiki: Boolean, publicRepo: Boolean) {
        createLocalAndRemote(parentDir, remoteRepoUserName, projectName, includeWiki, publicRepo, DefaultProjectCreator())
    }

    override fun createLocalAndRemote(parentDir: File, remoteRepoUserName: String, projectName: String, includeWiki: Boolean, publicRepo: Boolean, projectCreator: ProjectCreator) {
        remote.repoUser = remoteRepoUserName
        remote.repoName = projectName
        remote.create = true
        remote.publicProject = publicRepo

        local.projectDirParent = parentDir
        local.projectName = projectName
        local.cloneFromRemote = false
        local.create = true
        local.projectCreator = projectCreator

        wikiLocal.active = includeWiki
        wikiLocal.projectDirParent = parentDir
        wikiLocal.cloneFromRemote = false
        wikiLocal.create = true
    }


    override fun cloneFromRemote(cloneParentDir: File, remoteRepoUserName: String, projectName: String, includeWiki: Boolean) {
        cloneFromRemote(cloneParentDir, remoteRepoUserName, projectName, includeWiki, CloneExistsResponse.EXCEPTION)
    }

    override fun cloneFromRemote(cloneParentDir: File, remoteRepoUserName: String, projectName: String, includeWiki: Boolean, cloneExistsResponse: CloneExistsResponse) {
        remote.repoUser = remoteRepoUserName
        remote.repoName = projectName

        local.projectDirParent = cloneParentDir
        local.projectName = projectName
        local.cloneFromRemote = true
        local.create = false
        local.cloneExistsResponse = cloneExistsResponse

        wikiLocal.active = includeWiki
        wikiLocal.projectDirParent = cloneParentDir
        wikiLocal.cloneFromRemote = true
        wikiLocal.create = false
        wikiLocal.cloneExistsResponse = cloneExistsResponse
    }

    override fun propertiesFromGitPlus(): GitPlus {
        configuration.propertiesLoaders.clear()
        configuration.propertiesLoaders.add(FilePropertiesLoader().sourceFromGitPlus())
        return this
    }

    override fun propertiesFromGradle(): GitPlus {
        configuration.propertiesLoaders.clear()
        configuration.propertiesLoaders.add(FilePropertiesLoader().sourceFromGradle())
        return this
    }

    override fun evaluate() {
        remote = selectedRemote()
        local.prepare(this)
        wikiLocal.prepare(this)
        remote.prepare(this)
        log.debug("preparation stage complete")
    }


    override fun execute(): GitPlus {
        evaluate()
        if (log.isDebugEnabled) {
            val objectMapper = ObjectMapper()
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true)
            val swLocal = StringWriter()
            val swWiki = StringWriter()
            val swRemote = StringWriter()
            objectMapper.writeValue(swLocal, local.configuration)
            objectMapper.writeValue(swWiki, wikiLocal.configuration)
            objectMapper.writeValue(swRemote, remote.configuration)

            log.debug("executing GitPlus with configuration of: \nGitLocal:\n{}\nGitRemote:\n{}\nWikiLocal:\n{}", swLocal.toString(), swRemote.toString(), swWiki.toString())
        }
        try {
            propertiesResolver.loaders = ImmutableList.copyOf(configuration.propertiesLoaders)
            if (local.create && remote.create) {
                createBoth()
                processWiki()
                return this
            }
            if (remote.create) {
                remote.createRepo()
                return this
            }
            if (local.cloneFromRemote) {
                local.cloneRemote()
            } else if (local.create) {
                local.createAndInitialise()
            }
            processWiki()
        } catch (e: Exception) {
            throw GitPlusException("Failed to create or clone repository", e)
        }

        return this
    }

    private fun processWiki() {
        if (wikiLocal.active) {
            if (wikiLocal.cloneFromRemote) {
                wikiLocal.cloneRemote()
            } else if (wikiLocal.create) {
                wikiLocal.createAndInitialise()
                wikiLocal.setOrigin()
            }
        } else {
            log.debug("useWiki set to false, nothing done for the wiki")
        }
    }

    private fun selectedRemote(): GitRemote {
        return remoteResolver.get(serviceProvider, remote.configuration)
    }

    /**
     * Creates local repo, remote repo, master and develop branches, invokes the ProjectCreator (which by default only creates a README)
     * and pushes to remote.  Finishes with 'develop' branch selected
     */
    private fun createBoth() {
        log.debug("creating both local and remote repos")
        local.createAndInitialise()
        local.commit("Initial commit")
        remote.createRepo()
        local.setOrigin()
        local.push(false)
        local.checkoutNewBranch(GitBranch(DEVELOP_BRANCH))
    }


    companion object {
        val MASTER_BRANCH = "master"
        val DEVELOP_BRANCH = "develop"
        val REMOTE = "remote"
        val ORIGIN = "origin"
        val URL = "url"
    }
}