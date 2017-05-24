package uk.q3c.build.gitplus.local

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

/**
 * Created by David Sowerby on 30 Dec 2016
 */
class DefaultGitInitChecker : GitInitChecker {

    private var initDone = false
    private lateinit var git: Git

    /**
     * With thanks to: http://www.codeaffine.com/2014/09/22/access-git-repository-with-jgit/
     */
    override fun evaluateInitializationState() {

        val repositoryBuilder = FileRepositoryBuilder()
        repositoryBuilder.gitDir = git.repository.directory
        try {
            val repository = repositoryBuilder.build()
            initDone = (repository.findRef("HEAD") != null)
        } catch (e: RepositoryNotFoundException) {
            initDone = false
        }
    }

    /**
     * We keep a local flag for performance but if a check is requested explicitly and shows as not init'd, re-evaluate,
     * and only throw an exception if still not init'd
     */
    override fun checkInitDone() {
        if (!initDone) {
            evaluateInitializationState()
        }
        if (!initDone) {
            throw GitLocalException("Git repository has not been initialized")
        }
    }

    override fun isInitDone(): Boolean {
        return initDone
    }

    override fun initDone() {
        initDone = true
    }

    override fun reset() {
        initDone = false
    }

    override fun setGit(git: Git) {
        this.git = git
        evaluateInitializationState()
    }
}