package uk.q3c.build.gitplus.remote.github

import com.jcabi.github.RtGithub
import spock.lang.Specification
import uk.q3c.build.gitplus.remote.DefaultGitRemoteConfiguration
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.util.FileBuildPropertiesLoader

/**
 * Created by David Sowerby on 25 Mar 2016
 */
class GitHubServiceProviderTest extends Specification {

    def "get"() {
        given:
        DefaultGitRemoteConfiguration dummyConfiguration = new DefaultGitRemoteConfiguration()

        expect:
        new DefaultGitHubProvider(new FileBuildPropertiesLoader()).get(dummyConfiguration, GitRemote.TokenScope.RESTRICTED) instanceof RtGithub
        new DefaultGitHubProvider(new FileBuildPropertiesLoader()).get(dummyConfiguration, GitRemote.TokenScope.CREATE_REPO) instanceof RtGithub
        new DefaultGitHubProvider(new FileBuildPropertiesLoader()).get(dummyConfiguration, GitRemote.TokenScope.DELETE_REPO) instanceof RtGithub

    }
}
