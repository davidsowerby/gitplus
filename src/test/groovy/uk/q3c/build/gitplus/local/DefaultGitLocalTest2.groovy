package uk.q3c.build.gitplus.local

import org.eclipse.jgit.api.Git
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.remote.GitRemote

/**
 * Created by David Sowerby on 22 Dec 2016
 */
class DefaultGitLocalTest2 extends Specification {

    GitLocal gitLocal
    GitLocalConfiguration localConfiguration = Mock(GitLocalConfiguration)
    BranchConfigProvider branchConfigProvider = Mock(BranchConfigProvider)
    GitProvider gitProvider = Mock(GitProvider)
    GitRemote remote = Mock(GitRemote)
    Git git = Mock(Git)
    GitInitChecker mockInitChecker = Mock(GitInitChecker)
    GitPlus gitPlus = Mock(GitPlus)

    def setup() {
        gitPlus.remote >> remote
        gitLocal = new DefaultGitLocal(branchConfigProvider, gitProvider, localConfiguration, mockInitChecker, new DefaultGitCloner())
    }

    def "prepare does not validate if not active"() {

        when:
        gitLocal.prepare(gitPlus)

        then:
        1 * localConfiguration.getActive() >> true
        1 * gitProvider.openRepository(localConfiguration) >> git
        1 * localConfiguration.validate(remote)

        when:
        gitLocal.active = false
        gitLocal.prepare(gitPlus)

        then:
        1 * localConfiguration.getActive() >> false
        0 * localConfiguration.validate(remote)
    }

    def "close() before git property initialised does not error"() {

        when:
        gitLocal.close()

        then:
        noExceptionThrown()
    }
}
