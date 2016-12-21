package uk.q3c.build.gitplus.gitplus

import com.google.inject.Inject
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.local.WikiLocal
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.GitRemoteResolver
import uk.q3c.build.gitplus.remote.ServiceProvider
import java.io.File
import java.util.*


class DefaultGitPlus @Inject constructor(override val local: GitLocal,
                                         override val wikiLocal: WikiLocal,
                                         val remoteResolver: GitRemoteResolver) : GitPlus {

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


    override fun execute(): GitPlus {
        log.debug("executing GitPlus")
        remote = selectedRemote()
        local.prepare(remote)
        wikiLocal.prepare(remote, local)
        remote.prepare(local)
        log.debug("preparation stage complete")

        try {
            if (local.create && remote.create) {
                createLocalAndRemote()
                processWiki()
                return this
            }
            if (local.cloneFromRemote) {
                local.cloneRemote()
            } else if (local.create) {
                local.createAndInitialise()
            } else {
                local.verifyRemoteFromLocal()  // We are not creating anything just use it as it is
            }
            processWiki()
        } catch (e: Exception) {
            throw GitPlusException("Failed to create or verify repository", e)
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
            } else {
                wikiLocal.verifyRemoteFromLocal()  // We are not creating anything just use it as it is
            }
        } else {
            log.debug("useWiki set to false, nothing done for the wiki")
        }
    }

    private fun selectedRemote(): GitRemote {
        return remoteResolver.get(serviceProvider, remote.configuration)
    }

    /**
     * Creates local repo, remote repo, master and develop branches, a README, and if createProject is true also creates the project (and pushes to remote)
     * as well.  Finishes with 'develop' branch selected
     */
    private fun createLocalAndRemote() {
        log.debug("creating both local and remote repos")
        local.createAndInitialise()
        addReadmeToLocal()
        local.commit("Initial commit")
        remote.createRepo()
        local.setOrigin()
        local.push(false)
        local.checkoutNewBranch(GitBranch(DEVELOP_BRANCH))
    }


    /**
     * Creates a README file with just the project name in it, and adds the file to Git
     */
    private fun addReadmeToLocal() {
        val f = File(local.projectDir(), "README.md")
        val lines = ArrayList<String>()
        lines.add("# " + local.projectName)
        FileUtils.writeLines(f, lines)
        local.add(f)
    }


    companion object {
        val MASTER_BRANCH = "master"
        val DEVELOP_BRANCH = "develop"
        val REMOTE = "remote"
        val ORIGIN = "origin"
        val URL = "url"
    }
}