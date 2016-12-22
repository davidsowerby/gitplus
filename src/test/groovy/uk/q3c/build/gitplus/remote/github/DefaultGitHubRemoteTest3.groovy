package uk.q3c.build.gitplus.remote.github

import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.remote.DefaultGitRemoteConfiguration
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration
import uk.q3c.build.gitplus.remote.RemoteRequest

/**
 * Created by David Sowerby on 22 Dec 2016
 */
class DefaultGitHubRemoteTest3 extends Specification {

    DefaultGitHubRemote remote
    GitRemoteConfiguration configuration
    GitHubProvider gitHubProvider = Mock(GitHubProvider)
    RemoteRequest remoteRequest = Mock(RemoteRequest)
    GitHubUrlMapper urlMapper
    GitLocal gitLocal = Mock(GitLocal)

    def setup() {
        urlMapper = new GitHubUrlMapper()
        configuration = new DefaultGitRemoteConfiguration()
        remote = new DefaultGitHubRemote(configuration, gitHubProvider, remoteRequest, urlMapper)
    }


    def "prepare does nothing if not active"() {
        when:
        gitLocal.projectName >> 'wiggly'
        remote.prepare(gitLocal)

        then:
        thrown GitPlusConfigurationException

        when:
        remote.active = false
        remote.prepare(gitLocal)

        then:
        noExceptionThrown()


    }
}
