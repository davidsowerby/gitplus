package uk.q3c.gitplus.remote

import org.kohsuke.github.GitHub
import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
/**
 * Created by David Sowerby on 25 Mar 2016
 */
class GitHubServiceProviderTest extends Specification {

    def "get"() {
        given:
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration()

        expect:
        new GitHubProvider().get(dummyConfiguration, GitRemote.TokenScope.RESTRICTED) instanceof GitHub
        new GitHubProvider().get(dummyConfiguration, GitRemote.TokenScope.CREATE_REPO) instanceof GitHub
        new GitHubProvider().get(dummyConfiguration, GitRemote.TokenScope.DELETE_REPO) instanceof GitHub

    }
}
