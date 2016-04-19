package uk.q3c.build.gitplus.remote

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.jcabi.github.*
import com.jcabi.github.mock.MkGithub
import org.eclipse.jgit.transport.CredentialItem
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.build.gitplus.util.FileBuildPropertiesLoader

import javax.json.JsonObject
import javax.json.JsonReader

import static uk.q3c.build.gitplus.remote.GitRemote.ServiceProvider.GITHUB
import static uk.q3c.build.gitplus.remote.GitRemote.TokenScope.*

/**
 * Created by David Sowerby on 11 Mar 2016
 */
class GitHubRemoteTest extends Specification {


    final String USER = 'davidsowerby'
    Coordinates dummyCoordinates = new Coordinates.Simple(USER, 'dummy')


    GitRemote remote
    GitPlusConfiguration krailConfiguration
    GitPlusConfiguration scratchConfiguration
    GitPlusConfiguration emptyConfiguration
    GitPlusConfiguration dummyConfiguration

    Github gitHub = new MkGithub()
    GitHubProvider gitHubProvider = Mock(GitHubProvider)
    RemoteRequest remoteRequest = Mock(RemoteRequest)
    Repo repo


    def setup() {
        gitHub = new MkGithub(USER);
        Repos.RepoCreate repoCreate = new Repos.RepoCreate('dummy', false)
        repo = gitHub.repos().create(repoCreate)
        krailConfiguration = new GitPlusConfiguration().remoteRepoUser(USER).remoteRepoName('krail')
        scratchConfiguration = new GitPlusConfiguration().remoteRepoUser(USER).remoteRepoName('scratch')
        dummyConfiguration = new GitPlusConfiguration().remoteRepoUser(USER).remoteRepoName('dummy')
        emptyConfiguration = new GitPlusConfiguration()
    }


    def "construct with null throws NPE"() {

        when:
        remote = new GitHubRemote(null, gitHubProvider, remoteRequest)

        then:
        thrown NullPointerException

        when:
        remote = new GitHubRemote(scratchConfiguration, null, remoteRequest)

        then:
        thrown NullPointerException

        when:
        remote = new GitHubRemote(scratchConfiguration, gitHubProvider, null)

        then:
        thrown NullPointerException
    }


    def "fix words"() {
        given:
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)

        expect:
        remote.isIssueFixWord("fix")
        remote.isIssueFixWord("Fix")
        remote.isIssueFixWord("Fixes")
        remote.isIssueFixWord("Resolves")
        !remote.isIssueFixWord("Fixy")
        !remote.isIssueFixWord(null)

    }


    def "get issue, user repo and issue number"() {
        given:
        repo.issues().create('title', 'body')
        gitHubProvider.get(dummyConfiguration, RESTRICTED) >> gitHub
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        when:
        GPIssue issue = remote.getIssue(USER, 'dummy', 1)

        then:
        issue.getNumber() == 1

        when: //number not valid
        remote.getIssue(USER, 'dummy', 2)

        then:
        thrown GitRemoteException

        when: //repo not valid
        remote.getIssue(USER, 'rubbish', 1)

        then:
        thrown GitRemoteException
    }

    def "get issue, user repo and issue number, not current repo"() {
        given:
        repo.issues().create('title', 'body')
        gitHubProvider.get(scratchConfiguration, RESTRICTED) >> gitHub
        remote = new GitHubRemote(scratchConfiguration, gitHubProvider, remoteRequest)

        when:
        GPIssue issue = remote.getIssue(USER, 'dummy', 1)

        then:
        issue.getNumber() == 1
    }

    def "get issue, issue number only, assumes current repo"() {
        given:

        repo.issues().create('title', 'body')
        gitHubProvider.get(dummyConfiguration, RESTRICTED) >> gitHub
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        when:
        GPIssue issue = remote.getIssue(1)

        then:
        issue.getNumber() == 1
    }


    def "api status, all possible return values"() {
        given:
        gitHubProvider.get(krailConfiguration, RESTRICTED) >> gitHub
        JsonReader jsonReader = Mock(JsonReader)
        JsonObject jsonObject = Mock(JsonObject)
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)
        String apiKey = new FileBuildPropertiesLoader().apiTokenRestricted(GITHUB)

        when:
        GitHubRemote.Status result = remote.apiStatus()
        GitHubRemote.Status result1 = remote.apiStatus()
        GitHubRemote.Status result2 = remote.apiStatus()
        GitHubRemote.Status result3 = remote.apiStatus()

        then:
        4 * remoteRequest.request("GET", GitHubRemote.STATUS_API_URL, apiKey) >> jsonReader
        4 * jsonReader.readObject() >> jsonObject
        4 * jsonObject.getString("status") >>> ['good', 'minor', 'major', 'unexpected']
        result == GitHubRemote.Status.GREEN
        result1 == GitHubRemote.Status.YELLOW
        result2 == GitHubRemote.Status.RED
        result3 == GitHubRemote.Status.RED
    }


    def "api not available to return status"() {
        given:
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)
        String apiKey = new FileBuildPropertiesLoader().apiTokenRestricted(GITHUB)

        when:
        GitHubRemote.Status result = remote.apiStatus()

        then:
        1 * remoteRequest.request("GET", GitHubRemote.STATUS_API_URL, apiKey) >> { throw new IOException() }
        result == GitHubRemote.Status.RED
    }

    def "create issue"() {
        given:
        gitHubProvider.get(dummyConfiguration, RESTRICTED) >> gitHub
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)
        final String TITLE = 'title'
        final String BODY = 'body'
        final String[] LABELS = ['bug', 'build'] as String

        when:
        remote.createIssue(TITLE, BODY, LABELS)
        GPIssue result = remote.getIssue(1)

        then:
        result.getTitle().equals(TITLE)
        result.getBody().equals(BODY)
        result.getLabels().equals(ImmutableSet.copyOf(LABELS))
    }


    def "credentials provider"() {
        given:
        gitHubProvider.get(dummyConfiguration, RESTRICTED) >> gitHub
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        expect:
        remote.getCredentialsProvider() instanceof UsernamePasswordCredentialsProvider
        passwordMatches(remote.getCredentialsProvider() as UsernamePasswordCredentialsProvider, dummyConfiguration.getApiTokenRestricted())
    }


    def "credentials provider exception"() {
        given:
        GitPlusConfiguration mockConfiguration = Mock(GitPlusConfiguration)
        mockConfiguration.getApiTokenRestricted() >> { throw new IOException() }
        remote = new GitHubRemote(mockConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.getCredentialsProvider()

        then:
        thrown GitRemoteException
    }


    def "delete repo without confirmation throws GitRemoteException"() {
        given:
        Repos repos = Mock(Repos)
        Github gitHub1 = Mock(Github)
        gitHub1.repos() >> repos
        gitHubProvider.get(dummyConfiguration, RESTRICTED) >> gitHub1
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.deleteRepo()

        then:
        0 * gitHub1.repos()
        thrown GitRemoteException
    }

    def "delete repo"() {
        given:
        dummyConfiguration.confirmRemoteDelete("I really, really want to delete the davidsowerby/dummy repo from GitHub")
        gitHubProvider.get(dummyConfiguration, DELETE_REPO) >> gitHub
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)
        repo = gitHub.repos().get(dummyCoordinates) // make sure set up is correct

        when:
        remote.deleteRepo()
        repo = gitHub.repos().get(dummyCoordinates) // cause exception

        then:
        thrown IllegalArgumentException

    }

    def "create repo remote throws exception"() {
        given:
        Repos repos = Mock(Repos)
        repos.create(_) >> { throw new IOException() }
        Github gitHub1 = Mock(Github)
        gitHub1.repos() >> repos
        gitHubProvider.get(dummyConfiguration, CREATE_REPO) >> gitHub1

        dummyConfiguration.publicProject(true)
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.createRepo()

        then:
        thrown GitRemoteException
    }


    def "create repo successful, no label merge"() {
        given:
        gitHubProvider.get(dummyConfiguration, CREATE_REPO) >> gitHub
        gitHubProvider.get(dummyConfiguration, RESTRICTED) >> gitHub // for call to getLabelsAsMap()
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.createRepo()

        then:
        gitHub.repos().get(dummyCoordinates) != null
        remote.getLabelsAsMap().size() == 0
    }

    def "create repo successful, with label merge"() {
        given:
        dummyConfiguration.mergeIssueLabels(true)
        gitHubProvider.get(dummyConfiguration, CREATE_REPO) >> gitHub
        gitHubProvider.get(dummyConfiguration, RESTRICTED) >> gitHub // for call to mergeLabels()
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.createRepo()

        then:
        gitHub.repos().get(dummyCoordinates) != null
        remote.getLabelsAsMap().size() == 11
    }


    def "get urls"() {
        given:
        gitHubProvider.get(dummyConfiguration, RESTRICTED) >> gitHub
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        expect:
        remote.getTagUrl().equals("https://github.com/davidsowerby/dummy/tree")
        remote.getCloneUrl().equals("https://github.com/davidsowerby/dummy.git")
        remote.getHtmlUrl().equals("https://github.com/davidsowerby/dummy")

    }

    def "listRepoNames"() {
        given:
        createSomeRepos()
        gitHubProvider.get(dummyConfiguration, RESTRICTED) >> gitHub
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        expect:
        remote.listRepositoryNames().containsAll(ImmutableList.of('krail', 'krail-jpa', 'scratch', 'dummy'))
        remote.listRepositoryNames().size() == 4
    }

    def "merge labels"() {
        given:
        createLabels()
        gitHubProvider.get(dummyConfiguration, RESTRICTED) >> gitHub
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.mergeLabels()
        Labels labels = gitHub.repos().get(dummyCoordinates).labels()

        then:
        labels.get('bug')
        labels.get('question')

    }

    private void createLabels() {
        Repo r = gitHub.repos().get(dummyCoordinates)
        r.labels().create('bug', 'ee0701')
        r.labels().create('duplicate', 'cccccc')
        r.labels().create('enhancement', '84b6eb')
        r.labels().create('help wanted', '128a0c')
        r.labels().create('invalid', 'e6e6e6')
        r.labels().create('question', 'cc317c')
        r.labels().create('wontfix', 'ffffff')
    }


    boolean passwordMatches(UsernamePasswordCredentialsProvider credentialsProvider, String apiToken) {
        CredentialItem.Username username = new CredentialItem.Username()
        credentialsProvider.get(new URIish("??"), username)
        return apiToken.equals(username.getValue())
    }

    private void createSomeRepos() {
        List<String> repoNames = ImmutableList.of('krail', 'krail-jpa', 'scratch')
        for (String repoName : repoNames) {
            Repos.RepoCreate repoCreate = new Repos.RepoCreate(repoName, false)
            repo = gitHub.repos().create(repoCreate)
        }

    }

}