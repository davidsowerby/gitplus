package uk.q3c.gitplus

import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.remote.GitHubProvider
import uk.q3c.gitplus.remote.GitHubRemote
import uk.q3c.gitplus.remote.GitRemote
import uk.q3c.gitplus.remote.RemoteRequest
/**
 * This test needs to delete the 'dummy' repo in cleanup.  This test is a bit weird because it has to use deleteRepo to clean up, but also tests deleteRepo
 *
 * Created by David Sowerby on 20 Mar 2016
 */
class GitHubRemoteIntegrationTest2 extends Specification {

    GitPlusConfiguration krailConfiguration

    def setup() {
        krailConfiguration = new GitPlusConfiguration().remoteRepoFullName('davidsowerby/krail')
    }

    def "api status"() {
        given:
        GitRemote remote = new GitHubRemote(krailConfiguration, new GitHubProvider(), new RemoteRequest())

        expect:
        remote.apiStatus() == GitHubRemote.Status.GREEN
    }

    def "list repos for this user"() {
        given:
        GitRemote remote = new GitHubRemote(krailConfiguration, new GitHubProvider(), new RemoteRequest())


        when:
        Set<String> repos = remote.listRepositoryNames()

        then:
        repos.contains('krail')
        !repos.contains('perl')
        repos.size() == 16

    }


}