package uk.q3c.build.gitplus.local

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

/**
 * Created by David Sowerby on 06 Nov 2016
 */
class DefaultGitProvider : GitProvider {

    override fun openRepository(localConfiguration: GitLocalConfiguration): Git {
        val file = File(localConfiguration.projectDir(), ".git")
        val builder = FileRepositoryBuilder()
        val repo = builder.setGitDir(file).readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build()
        return Git(repo)
    }
}