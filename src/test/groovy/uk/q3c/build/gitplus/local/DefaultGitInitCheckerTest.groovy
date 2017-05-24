package uk.q3c.build.gitplus.local

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Repository
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Created by David Sowerby on 17 May 2017
 */
class DefaultGitInitCheckerTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp
    File projectDir
    Git git = Mock()

    DefaultGitInitChecker initChecker
    Repository repo = Mock()
    File gitDir

    def setup() {
        initChecker = new DefaultGitInitChecker()
        temp = temporaryFolder.getRoot()
        projectDir = new File(temp, "wiggly")
        git.repository >> repo
        gitDir = new File(projectDir, ".git")
        repo.directory >> gitDir
    }

    def "reset"() {
        given:
        initChecker.initDone()

        when:
        initChecker.reset()

        then:
        !initChecker.initDone
    }

    def "init not done, re-evaluate"() {
        given: "repo directory exists but not init'd"
        projectDir = new File(temp, "wiggly")
        initChecker.setGit(git)


        when: "init checked"
        initChecker.checkInitDone()

        then: "exception thrown"
        thrown GitLocalException

        when: "init the repo, and check init again"
        Repository repo = null
        repo = new FileRepository(gitDir)
        repo.create()
        initChecker.checkInitDone()

        then: "shows as init'd"
        initChecker.initDone
    }
}
