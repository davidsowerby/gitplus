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
    }
    /**
     * Delete repo using full access key
     */
    private void deleteRepo() {
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration()
                .publicProject(true)
                .remoteRepoFullName('davidsowerby/dummy')
                .confirmRemoteDelete("I really, really want to delete the davidsowerby/dummy repo from GitHub")
        GitRemote remote = new GitHubRemote(dummyConfiguration, new GitHubProvider(), new RemoteRequest())
        println 'deleting repo'
        remote.deleteRepo()
    }


/**
 * returns as soon as it fails to find repoName (checking for absence / deletion).  Use waitRemoteRepoExists for fast return of found
 * @param repoName
 * @return
 */
    private void waitRemoteRepoNotExists(String repoName) {
        println 'waiting for repo not to exist'
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration()
                .remoteRepoFullName('davidsowerby/dummy')
        GitRemote remote = new GitHubRemote(dummyConfiguration, new GitHubProvider(), new RemoteRequest())
        def timeout = 20
        Set<String> names = remote.listRepositoryNames()
        while (names.contains(repoName) && timeout > 0) {
            println 'waiting 1 second for api, ' + timeout + ' before timeout'
            Thread.sleep(1000)
            timeout--
            names = remote.listRepositoryNames()
        }
        if (timeout) {
            throw new RuntimeException("Timed out")
        }
    }

/**
 * returns as soon as it finds repoName.  Use waitRemoteRepoNotExists for fast return of not found
 * @param repoName
 * @return
 */
    private void waitRemoteRepoExists(String repoName) {
        println 'waiting for repo to exist'
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration()
                .remoteRepoFullName('davidsowerby/dummy')
        GitRemote remote = new GitHubRemote(dummyConfiguration, new GitHubProvider(), new RemoteRequest())
        def timeout = 20
        Set<String> names = remote.listRepositoryNames()
        while (!names.contains(repoName) && timeout > 0) {
            println 'waiting 1 second for api, ' + timeout + ' before timeout'
            Thread.sleep(1000)
            timeout--
            names = remote.listRepositoryNames()
        }
        if (timeout <= 0) {
            throw new RuntimeException("Timed out")
        }
    }


}