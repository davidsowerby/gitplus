package uk.q3c.gitplus

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHIssueBuilder
import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.local.GitLocal
import uk.q3c.gitplus.local.GitLocalException
import uk.q3c.gitplus.local.PushResponse
import uk.q3c.gitplus.remote.GitHubRemote
import uk.q3c.gitplus.remote.GitRemote
import uk.q3c.gitplus.util.UserHomeBuildPropertiesLoader

/**
 * This test needs to delete the 'dummy' repo in cleanup.  This test is a bit weird because it has to use deleteRepo to clean up, but also tests deleteRepo
 *
 * Created by David Sowerby on 20 Mar 2016
 */
class GitHubRemoteIntegrationTest extends Specification {

    String fullAccessApiKey
    String apiKey

    def setup() {
        def loader = new UserHomeBuildPropertiesLoader();
        loader.load()
        fullAccessApiKey = loader.githubKeyFullAccess()
        apiKey = loader.githubKeyRestricted()
        println 'cleaning up at start'
        if (remoteRepoExists('dummy')) {
            deleteRepo()
            waitRemoteRepoNotExists('dummy')
        }

    }

    def cleanup() {
        println 'cleaning up at end'
        if (remoteRepoExists('dummy')) {
            deleteRepo()
            waitRemoteRepoNotExists('dummy')
        }

    }

    def "api status"() {
        given:
        remote = new GitHubRemote(krailConfiguration, gitHubProvider)

        expect:
        remote.apiStatus() == GitHubRemote.Status.GREEN
    }

    def "list repos for this user"() {
        given:
        remote = new GitHubRemote(krailConfiguration, gitHubProvider)


        when:
        Set<String> repos = remote.listRepositoryNames()

        then:
        repos.contains('krail')
        !repos.contains('perl')
    }

    def "create issue"() {
        given:
        remote = new GitHubRemote(scratchConfiguration, gitHubProvider)
        String title = "test issue"
        String body = "body"
        String label = "buglet"
        GHIssueBuilder issueBuilder = Mock(GHIssueBuilder)

        when:
        GHIssue result = remote.createIssue(title, body, label)

        then:
        result.getNumber() > 0
        result.getTitle().equals(title)
        result.getBody().equals(body)
        result.getLabels().size() == 1
        containsLabel(result.getLabels(), label)
    }

    def "create repo with correct configuration"() {
        given:
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration().apiToken(fullAccessApiKey)
                .publicProject(true).remoteRepoFullName('davidsowerby/dummy')
        GitRemote remote = new GitHubRemote(dummyConfiguration)

        when:
        remote.createRepo()
        println 'created repo'

        then:
        remote.getRepo().getName().equals("dummy")
        waitRemoteRepoExists("dummy")
        println 'repo exists'


        when:
        println 'pause for breath'
        Thread.sleep(4000) //sometimes fails if we rush straight in to delete
        deleteRepo()
        println 'deleted'

        then:
        waitRemoteRepoNotExists("dummy")
    }

    void deleteRepo() {
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration().apiToken(fullAccessApiKey)
                .publicProject(true).remoteRepoFullName('davidsowerby/dummy')
                .confirmRemoteDelete("I really, really want to delete the davidsowerby/dummy repo from GitHub")
        GitRemote remote = new GitHubRemote(dummyConfiguration)
        println 'deleting repo'
        remote.deleteRepo()
    }

/**
 * returns as soon as it finds repoName.  Use waitRemoteRepoNotExists for fast return of not found
 * @param repoName
 * @return
 */
    private void waitRemoteRepoExists(String repoName) {
        println 'waiting for repo to exist'
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration().apiToken(apiKey)
                .remoteRepoFullName('davidsowerby/dummy')
        GitRemote remote = new GitHubRemote(dummyConfiguration)
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

    /**
     * Requires a remote repo to clone
     */
    def "clone remote and push"() {
        given:
        configuration.projectName("scratch").projectDirParent(temp).remoteRepoFullName('davidsowerby/scratch').validate()
        gitLocal = new GitLocal(configuration)
        String apiToken = new UserHomeBuildPropertiesLoader().load().githubKeyRestricted()
        gitRemote.getCredentialsProvider() >> new UsernamePasswordCredentialsProvider(apiToken, "")

        when:
        gitLocal.cloneRemote()

        then:
        new File(temp, "scratch").exists()
        gitLocal.getOrigin().equals(configuration.getRemoteRepoUrl() + ".git")

        when:
        addAFile();
        gitLocal.commit("xx")
        PushResponse result = gitLocal.push(gitRemote, true)

        then:
        result.isSuccessful()
    }

    def "clone failure throws GitLocalException"() {
        given:
        //use invalid url
        configuration.projectName("scratch").projectDirParent(temp).remoteRepoFullName("davidsowerby/scrtch")
        configuration.validate()
        gitLocal = new GitLocal(configuration)

        when:
        gitLocal.cloneRemote()

        then:
        thrown GitLocalException
    }

/**
 * returns as soon as it fails to find repoName (checking for absence / deletion).  Use waitRemoteRepoExists for fast return of found
 * @param repoName
 * @return
 */
    private void waitRemoteRepoNotExists(String repoName) {
        println 'waiting for repo not to exist'
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration().apiToken(apiKey)
                .remoteRepoFullName('davidsowerby/dummy')
        GitRemote remote = new GitHubRemote(dummyConfiguration)
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

    boolean remoteRepoExists(String repoName) {
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration().apiToken(apiKey)
                .remoteRepoFullName('davidsowerby/dummy')
        GitRemote remote = new GitHubRemote(dummyConfiguration)
        println 'getting repo names'
        Set<String> names = remote.listRepositoryNames()
        println 'names retrieved'
        return names.contains(repoName)
    }

}