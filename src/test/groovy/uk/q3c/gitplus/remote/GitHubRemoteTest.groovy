package uk.q3c.gitplus.remote

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import org.eclipse.jgit.transport.CredentialItem
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.kohsuke.github.*
import spock.lang.Specification
import spock.lang.Unroll
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.util.UserHomeBuildPropertiesLoader

import javax.json.JsonObject
import javax.json.JsonReader

/**
 * Created by David Sowerby on 11 Mar 2016
 */
class GitHubRemoteTest extends Specification {

    GitRemote remote
    GitPlusConfiguration krailConfiguration
    GitPlusConfiguration scratchConfiguration
    GitPlusConfiguration emptyConfiguration
    GitPlusConfiguration dummyConfiguration
    String apiKey
    GitHub gitHub = Mock(GitHub)
    GitHubProvider gitHubProvider = Mock(GitHubProvider)
    GHRepository repo = Mock(GHRepository)
    Issue issue1 = Mock(Issue)
    GHIssue ghIssue1 = Mock(GHIssue)
    GHLabel ghLabel = Mock(GHLabel)
    URL htmlUrl1 = new URL("https://github.com/davidsowerby/scratch/issues/1")
    RemoteRequest remoteRequest = Mock(RemoteRequest)


    def setup() {
        def loader = new UserHomeBuildPropertiesLoader();
        loader.load()
        apiKey = loader.githubKeyRestricted()
        krailConfiguration = new GitPlusConfiguration().apiToken(apiKey).remoteRepoFullName('davidsowerby/krail')
        scratchConfiguration = new GitPlusConfiguration().apiToken(apiKey).remoteRepoFullName('davidsowerby/scratch')
        dummyConfiguration = new GitPlusConfiguration().apiToken(apiKey).remoteRepoFullName('davidsowerby/dummy')
        emptyConfiguration = new GitPlusConfiguration()
        gitHubProvider.get(_) >> gitHub
        ghIssue1.getLabels() >> ImmutableSet.of(ghLabel)
        ghIssue1.getHtmlUrl() >> htmlUrl1
        ghIssue1.getNumber() >> 1

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

    @Unroll
    def "get issue, number only, or null or empty repo name"() {
        given:
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)

        when:
        Issue issue = remote.getIssue(rname, 1)

        then:
        1 * gitHub.getRepository('davidsowerby/krail') >> repo
        1 * repo.getIssue(1) >> ghIssue1
        issue.getNumber() == 1


        where:
        rname                | _
        'davidsowerby/krail' | _
        null                 | _
        ""                   | _
    }

    def "get issue, number only"() {
        given:
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)

        when:
        Issue issue = remote.getIssue(1)

        then:
        1 * gitHub.getRepository('davidsowerby/krail') >> repo
        1 * repo.getIssue(1) >> ghIssue1

    }

    def "get issues, invalid repo"() {
        given:
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.getIssue(1)

        then:
        1 * gitHub.getRepository('davidsowerby/krail') >> { throw new IOException() }
        thrown GitRemoteException
    }


    def "api status, all possible return values"() {
        given:
        JsonReader jsonReader = Mock(JsonReader)
        JsonObject jsonObject = Mock(JsonObject)
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)

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
        JsonReader jsonReader = Mock(JsonReader)
        JsonObject jsonObject = Mock(JsonObject)
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)

        when:
        GitHubRemote.Status result = remote.apiStatus()

        then:
        1 * remoteRequest.request("GET", GitHubRemote.STATUS_API_URL, apiKey) >> { throw new IOException() }
        result == GitHubRemote.Status.RED
    }


    def "create issue"() {
        given:
        remote = new GitHubRemote(scratchConfiguration, gitHubProvider, remoteRequest)
        String title = "test issue"
        String body = "body"
        String label = "buglet"
        GHMyself ghUser = Mock(GHMyself)
        GHIssueBuilder issueBuilder = Mock(GHIssueBuilder)


        when:
        GHIssue result = remote.createIssue(title, body, label)

        then:
        1 * gitHub.getRepository('davidsowerby/scratch') >> repo
        1 * repo.createIssue(title) >> issueBuilder
        1 * issueBuilder.body(body) >> issueBuilder
        1 * gitHub.getMyself() >> ghUser
        1 * issueBuilder.assignee(ghUser) >> issueBuilder
        1 * issueBuilder.label(label) >> issueBuilder
        1 * issueBuilder.create() >> ghIssue1
        result == ghIssue1
    }

    def "credentials provider"() {
        given:
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)

        expect:
        remote.getCredentialsProvider() instanceof UsernamePasswordCredentialsProvider
        passwordMatches(remote.getCredentialsProvider() as UsernamePasswordCredentialsProvider, krailConfiguration.getApiToken())
    }


    def "credentials provider exception"() {
        given:
        remote = new GitHubRemote(emptyConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.getCredentialsProvider()

        then:
        thrown GitRemoteException
    }

    def "delete repo without confirmation throws GitRemoteException"() {
        given:
        remote = new GitHubRemote(scratchConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.deleteRepo()

        then:
        0 * gitHub.getRepository(_)
        thrown GitRemoteException
    }

    def "delete repo"() {
        given:
        dummyConfiguration.confirmRemoteDelete("I really, really want to delete the davidsowerby/dummy repo from GitHub").apiToken('xxx')
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.deleteRepo()

        then:
        1 * gitHubProvider.get(_) >> gitHub
        1 * gitHub.getRepository('davidsowerby/dummy') >> repo
        1 * repo.delete()
    }


    def "create repo remote throws exception"() {
        given:
        dummyConfiguration.apiToken(apiKey).publicProject(true).remoteRepoFullName('davidsowerby/dummy')
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.createRepo()

        then:
        1 * gitHub.createRepository('dummy', '', null, true) >> { throw new IOException() }
        thrown GitRemoteException
    }

    def "create repo successful"() {
        given:
        dummyConfiguration.apiToken('xxx')
        remote = new GitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest)

        when:
        remote.createRepo()

        then:
        1 * gitHubProvider.get(_) >> gitHub
        1 * gitHub.createRepository('dummy', '', null, false) >> repo
        remote.getRepo() == repo
    }


    def "list repos for this user"() {
        given:
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)
        GHMyself ghMyself = Mock(GHMyself)
        GHRepository ghRepository1 = Mock(GHRepository)
        GHRepository ghRepository2 = Mock(GHRepository)
        Map<String, GHRepository> repos = ImmutableMap.of('repo1', ghRepository1, 'repo2', ghRepository2)

        when:
        Set<String> result = remote.listRepositoryNames()

        then:
        1 * gitHub.getMyself() >> ghMyself
        1 * ghMyself.getRepositories() >> repos
        result.containsAll(ImmutableList.of('repo1', 'repo2'))
        result.size() == 2
    }

    def "get tag url"() {
        given:
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)
        URL url = new URL("https://github.com/davidsowerby/krail")

        when:
        String result = remote.getTagUrl()

        then:
        1 * gitHub.getRepository('davidsowerby/krail') >> repo
        1 * repo.getHtmlUrl() >> url
        result.equals("https://github.com/davidsowerby/krail/tree")

    }

    def "get http url and clone url"() {
        given:
        remote = new GitHubRemote(krailConfiguration, gitHubProvider, remoteRequest)
        URL url = new URL("https://github.com/davidsowerby/krail")

        when:
        String htmlUrl = remote.getHtmlUrl()
        String cloneUrl = remote.getCloneUrl()

        then:
        1 * gitHub.getRepository('davidsowerby/krail') >> repo
        2 * repo.getHtmlUrl() >> url
        htmlUrl.equals("https://github.com/davidsowerby/krail")
        cloneUrl.equals("https://github.com/davidsowerby/krail.git")

    }


    boolean passwordMatches(UsernamePasswordCredentialsProvider credentialsProvider, String apiToken) {
        CredentialItem.Username username = new CredentialItem.Username()
        credentialsProvider.get(new URIish("??"), username)
        return apiToken.equals(username.getValue())
    }

    boolean containsLabel(Collection<GHLabel> ghLabels, String labelName) {
        for (GHLabel label : ghLabels) {
            if (label.name.equals(labelName)) {
                return true
            }
        }
        return false
    }


}