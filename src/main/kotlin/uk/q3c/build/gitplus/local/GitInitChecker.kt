package uk.q3c.build.gitplus.local

import org.eclipse.jgit.api.Git

/**
 * Created by David Sowerby on 30 Dec 2016
 */
interface GitInitChecker {
    fun evaluateInitializationState()
    fun reset()
    fun initDone()
    fun isInitDone(): Boolean
    fun checkInitDone()
    fun setGit(git: Git)
}