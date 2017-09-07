package uk.q3c.build.gitplus.remote.github

import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.remote.DefaultGitRemoteConfiguration
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration
import uk.q3c.build.gitplus.remote.RemoteRequest
import uk.q3c.build.gitplus.util.PropertiesResolver

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
    PropertiesResolver propertiesResolver = Mock(PropertiesResolver)
    GitPlus gitPlus = Mock(GitPlus)


    def setup() {
        urlMapper = new GitHubUrlMapper()
        configuration = new DefaultGitRemoteConfiguration()
        remote = new DefaultGitHubRemote(configuration, gitHubProvider, remoteRequest, urlMapper)
        gitPlus.remote >> remote
        gitPlus.local >> gitLocal
    }


    def "prepare does nothing if not active"() {
        when:
        gitLocal.projectName >> 'wiggly'
        remote.prepare(gitPlus)

        then:
        thrown GitPlusConfigurationException

        when:
        remote.active = false
        remote.prepare(gitPlus)

        then:
        noExceptionThrown()


    }
}
