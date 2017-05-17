package uk.q3c.build.gitplus.local

import org.eclipse.jgit.api.Git
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Encapsulates a Git CloneCommand to facilitate testing
 *
 * Created by David Sowerby on 17 May 2017
 */
class DefaultGitCloner : GitCloner {
    private val log = LoggerFactory.getLogger(this.javaClass.name)

    override fun doClone(localDir: File, remoteUrl: String, gitInitChecker: GitInitChecker) {
        log.debug("cloning remote from: {}", remoteUrl)
        Git.cloneRepository().setURI(remoteUrl).setDirectory(localDir).call()
        gitInitChecker.initDone()
    }
}

interface GitCloner {

    fun doClone(localDir: File, remoteUrl: String, gitInitChecker: GitInitChecker)
}
