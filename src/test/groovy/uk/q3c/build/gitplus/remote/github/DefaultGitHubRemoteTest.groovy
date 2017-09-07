package uk.q3c.build.gitplus.remote.github

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.jcabi.github.*
import com.jcabi.github.mock.MkGithub
import org.apache.commons.codec.digest.DigestUtils
import org.eclipse.jgit.transport.CredentialItem
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import spock.lang.Specification
import uk.q3c.build.gitplus.GitSHA
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.remote.*
import uk.q3c.build.gitplus.util.APIProperty
import uk.q3c.build.gitplus.util.FilePropertiesLoader

import javax.json.JsonObject
import javax.json.JsonReader

/**
 * Created by David Sowerby on 11 Mar 2016
 */
class DefaultGitHubRemoteTest extends Specification {


    final String USER = 'davidsowerby'
    Coordinates dummyCoordinates = new Coordinates.Simple(USER, 'dummy')


    GitRemote remote
    GitRemoteConfiguration krailConfiguration
    GitRemoteConfiguration scratchConfiguration
    GitRemoteConfiguration emptyConfiguration
    GitRemoteConfiguration dummyConfiguration

    Github gitHub = new MkGithub()
    GitHubProvider gitHubProvider = Mock(GitHubProvider)
    RemoteRequest remoteRequest = Mock(RemoteRequest)
    Repo repo
    GitPlus gitPlus = Mock(GitPlus)
    GitLocal gitLocal = Mock(GitLocal)


    def setup() {

        gitHub = new MkGithub(USER)
        Repos.RepoCreate repoCreate = new Repos.RepoCreate('dummy', false)
        repo = gitHub.repos().create(repoCreate)
        krailConfiguration = new DefaultGitRemoteConfiguration().repoUser(USER).repoName('krail')
        scratchConfiguration = new DefaultGitRemoteConfiguration().repoUser(USER).repoName('scratch')
        dummyConfiguration = new DefaultGitRemoteConfiguration().repoUser(USER).repoName('dummy')
        emptyConfiguration = new DefaultGitRemoteConfiguration()
        gitPlus.local >> gitLocal

    }

    def "headCommit, also checks branch not found"() {
        given:
        Github mockGitHub = Mock(Github)
        Repos mockRepos = Mock(Repos)
        Repo mockRepo = Mock(Repo)
        Branches branches = Mock(Branches)

        Branch masterBranch = Mock(Branch)
        Branch developBranch = Mock(Branch)
        Branch featureBranch = Mock(Branch)
        masterBranch.name() >> 'master'
        developBranch.name() >> 'develop'
        featureBranch.name() >> 'feature'
        Iterable<Branch> branchIterable = ImmutableList.of(masterBranch, developBranch, featureBranch)
        Commit commit = Mock(Commit)
        GitSHA expectedSHA = new GitSHA(DigestUtils.sha1Hex('wiggly'))
        commit.sha() >> expectedSHA
        developBranch.commit() >> commit

        mockGitHub.repos() >> mockRepos
        mockRepos.get(new Coordinates.Simple(USER, 'dummy')) >> mockRepo
        mockRepo.branches() >> branches
        branches.iterate() >> branchIterable
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> mockGitHub
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)


        when:
        GitSHA hash1 = remote.headCommit(new GitBranch('develop'))
        GitSHA hash2 = remote.developHeadCommit()

        then:
        hash1 != null
        hash1 == expectedSHA
        hash2 == expectedSHA

        when:
        remote.getBranch(new GitBranch('wiggly'))

        then:
        thrown IOException

        when:
        boolean hasDevelop = remote.hasBranch(new GitBranch("develop"))
        boolean hasWiggly = remote.hasBranch(new GitBranch("wiggly"))

        then:
        hasDevelop
        !hasWiggly

    }

    def "prepare calls validate"() {
        given:
        GitRemoteConfiguration configuration = new DefaultGitRemoteConfiguration()
        remote = new DefaultGitHubRemote(configuration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        gitLocal.projectName >> 'wiggly'

        when:
        remote.prepare(gitPlus)

        then: "validate fails"
        thrown GitPlusConfigurationException
    }

    def "construct with null throws IllegalArgumentException"() {

        when:
        remote = new DefaultGitHubRemote(null, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.propertiesResolver = gitPlus

        then:
        thrown IllegalArgumentException

        when:
        remote = new DefaultGitHubRemote(scratchConfiguration, null, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        then:
        thrown IllegalArgumentException

        when:
        remote = new DefaultGitHubRemote(scratchConfiguration, gitHubProvider, null, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        then:
        thrown IllegalArgumentException
    }


    def "fix words"() {
        given:
        remote = new DefaultGitHubRemote(krailConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        expect:
        remote.isIssueFixWord("fix")
        remote.isIssueFixWord("Fix")
        remote.isIssueFixWord("Fixes")
        remote.isIssueFixWord("Resolves")
        !remote.isIssueFixWord("Fixy")

    }


    def "get issue, user repo and issue number"() {
        given:
        repo.issues().create('title', 'body')
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

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
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub
        remote = new DefaultGitHubRemote(scratchConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        when:
        GPIssue issue = remote.getIssue(USER, 'dummy', 1)

        then:
        issue.getNumber() == 1
    }

    def "get issue, issue number only, assumes current repo"() {
        given:

        repo.issues().create('title', 'body')
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        when:
        GPIssue issue = remote.getIssue(1)

        then:
        issue.getNumber() == 1
    }


    def "api status, all possible return values"() {
        given:
        String apiKey = new FilePropertiesLoader().getPropertyValue(APIProperty.ISSUE_CREATE_TOKEN, ServiceProvider.GITHUB)
        gitPlus.apiTokenIssueCreate(ServiceProvider.GITHUB) >> apiKey
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub
        JsonReader jsonReader = Mock(JsonReader)
        JsonObject jsonObject = Mock(JsonObject)
        remote = new DefaultGitHubRemote(krailConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        when:
        DefaultGitHubRemote.Status result = remote.apiStatus()
        DefaultGitHubRemote.Status result1 = remote.apiStatus()
        DefaultGitHubRemote.Status result2 = remote.apiStatus()
        DefaultGitHubRemote.Status result3 = remote.apiStatus()

        then:
        4 * remoteRequest.request("GET", GitHubUrlMapper.STATUS_API_URL, apiKey) >> jsonReader
        4 * jsonReader.readObject() >> jsonObject
        4 * jsonObject.getString("status") >>> ['good', 'minor', 'major', 'unexpected']
        result == DefaultGitHubRemote.Status.GREEN
        result1 == DefaultGitHubRemote.Status.YELLOW
        result2 == DefaultGitHubRemote.Status.RED
        result3 == DefaultGitHubRemote.Status.RED
    }


    def "api not available to return status"() {
        given:
        String apiKey = new FilePropertiesLoader().getPropertyValue(APIProperty.ISSUE_CREATE_TOKEN, ServiceProvider.GITHUB)
        gitPlus.apiTokenIssueCreate(ServiceProvider.GITHUB) >> apiKey
        remote = new DefaultGitHubRemote(krailConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        when:
        DefaultGitHubRemote.Status result = remote.apiStatus()

        then:
        1 * remoteRequest.request("GET", GitHubUrlMapper.STATUS_API_URL, apiKey) >> { throw new IOException() }
        result == DefaultGitHubRemote.Status.RED
    }

    def "create issue"() {
        given:
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)
        final String TITLE = 'title'
        final String BODY = 'body'
        final String[] LABELS = ['bug', 'build'] as String

        when:
        remote.createIssue(TITLE, BODY, LABELS)
        GPIssue result = remote.getIssue(1)

        then:
        result.getTitle() == TITLE
        result.getBody() == BODY
        result.getLabels() == ImmutableSet.copyOf(LABELS)
    }


    def "credentials provider"() {
        given:
        String apiKey = new FilePropertiesLoader().getPropertyValue(APIProperty.ISSUE_CREATE_TOKEN, ServiceProvider.GITHUB)
        gitPlus.apiTokenIssueCreate(ServiceProvider.GITHUB) >> apiKey
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        expect:
        remote.getCredentialsProvider() instanceof UsernamePasswordCredentialsProvider
        passwordMatches(remote.getCredentialsProvider() as UsernamePasswordCredentialsProvider, gitPlus.apiTokenIssueCreate(ServiceProvider.GITHUB))
    }


    def "credentials provider exception"() {
        given:
        GitRemoteConfiguration mockConfiguration = Mock(GitRemoteConfiguration)
        gitPlus.apiTokenIssueCreate(ServiceProvider.GITHUB) >> { throw new IOException("Fake exception") }
        remote = new DefaultGitHubRemote(mockConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

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
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub1
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        when:
        remote.deleteRepo()

        then:
        0 * gitHub1.repos()
        thrown GitRemoteException
    }

    def "delete repo"() {
        given:
        dummyConfiguration.confirmDelete("I really, really want to delete the davidsowerby/dummy repo from GitHub")
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.DELETE_REPO) >> gitHub
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)
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
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_REPO) >> gitHub1

        dummyConfiguration.publicProject(true)
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        when:
        remote.createRepo()

        then:
        thrown GitRemoteException
    }


    def "create repo successful, no label merge"() {
        given:
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_REPO) >> gitHub
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub // for call to getLabelsAsMap()
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        when:
        remote.createRepo()

        then:
        gitHub.repos().get(dummyCoordinates) != null
        remote.getLabelsAsMap().size() == 0
    }

    def "create repo successful, with label merge"() {
        given:
        dummyConfiguration.mergeIssueLabels(true)
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_REPO) >> gitHub
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub // for call to mergeLabels()
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        when:
        remote.createRepo()

        then:
        gitHub.repos().get(dummyCoordinates) != null
        remote.getLabelsAsMap().size() == 11
    }


    def "get urls"() {
        given:
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        expect:
        remote.tagUrl() == "https://github.com/davidsowerby/dummy/tree/"
        remote.cloneUrl() == "https://github.com/davidsowerby/dummy.git"
        remote.repoBaselUrl() == "https://github.com/davidsowerby/dummy"
        remote.apiUrl() == "https://api.github.com"
        remote.issuesUrl() == "https://github.com/davidsowerby/dummy/issues/"
        remote.wikiUrl() == "https://github.com/davidsowerby/dummy/wiki"
        remote.wikiCloneUrl() == "https://github.com/davidsowerby/dummy.wiki.git"

    }

    def "listRepoNames"() {
        given:
        createSomeRepos()
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        expect:
        remote.listRepositoryNames().containsAll(ImmutableList.of('krail', 'krail-jpa', 'scratch', 'dummy'))
        remote.listRepositoryNames().size() == 4
    }

    def "merge labels"() {
        given:
        createLabels()
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)

        when:
        remote.mergeLabels()
        Labels labels = gitHub.repos().get(dummyCoordinates).labels()

        then:
        labels.get('bug')
        labels.get('question')

    }

    def "verifyFromLocal"() {
        given:
        gitHubProvider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) >> gitHub
        remote = new DefaultGitHubRemote(dummyConfiguration, gitHubProvider, remoteRequest, new GitHubUrlMapper())
        remote.prepare(gitPlus)
        String origin = "https://github.com/davidsowerby/gitplus"

        when:
        remote.prepare(gitPlus)
        remote.verifyFromLocal()

        then:
        1 * gitLocal.active >> false
        thrown GitPlusConfigurationException

        when:
        remote.prepare(gitPlus)
        remote.verifyFromLocal()

        then:
        1 * gitLocal.active >> true
        1 * gitLocal.getOrigin() >> origin
        remote.cloneUrl() == "${origin}.git"


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