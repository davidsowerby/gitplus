package uk.q3c.build.gitplus.local

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PushCommand
import org.eclipse.jgit.lib.BranchConfig
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.StoredConfig
import org.eclipse.jgit.transport.CredentialsProvider
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.remote.GitRemote
/**
 * Created by David Sowerby on 30 Oct 2016
 */
class DefaultWikiLocalTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    Git mockGit = Mock(Git)
    DefaultWikiLocal wikiLocal
    GitLocalConfiguration localConfiguration
    GitRemote gitRemote = Mock(GitRemote)
    GitLocal gitLocal = Mock(GitLocal)
    DefaultGitLocalConfiguration configuration
    GitProvider gitProvider
    GitProvider mockGitProvider = Mock(GitProvider)
    BranchConfigProvider branchConfigProvider = Mock(BranchConfigProvider)
    BranchConfig branchConfig = Mock(BranchConfig)
    Repository repository = Mock(Repository)
    StoredConfig repoConfig = Mock(StoredConfig)
    Set<String> remotes = Mock(Set)
    GitInitChecker mockInitChecker = Mock(GitInitChecker)
    MockGitCloner cloner

    def setup() {
        cloner = new MockGitCloner()
        temp = temporaryFolder.getRoot()
        localConfiguration = new DefaultGitLocalConfiguration()
        gitLocal.getLocalConfiguration() >> localConfiguration
        branchConfigProvider.get(_, _) >> branchConfig
        mockGitProvider.openRepository(_) >> mockGit
        wikiLocal = new DefaultWikiLocal(branchConfigProvider, mockGitProvider, new DefaultGitLocalConfiguration(), mockInitChecker, cloner)
        wikiLocal.active = true
    }

    def "prepare modifies copies relevant config"() {
        given:
        localConfiguration.projectName('wiggly').projectDirParent(temp)
        gitLocal.getProjectName() >> 'wiggly'
        gitLocal.getProjectDirParent() >> temp
        gitLocal.getTaggerEmail() >> 'me@there'
        gitLocal.getTaggerName() >> 'me'

        when:
        wikiLocal.prepare(gitRemote, gitLocal)

        then:
        wikiLocal.projectName == 'wiggly.wiki'
        wikiLocal.projectDir() == new File(temp, 'wiggly.wiki')
        wikiLocal.taggerName == 'me'
        wikiLocal.taggerEmail == 'me@there'
    }


    def "push"() {
        given:
        PushCommand pc = Mock(PushCommand)
        CredentialsProvider credentialsProvider = Mock(CredentialsProvider)
        gitRemote.getCredentialsProvider() >> credentialsProvider
        mockGit.push() >> pc
        pc.call() >> { throw new GitLocalException('broken') }

        when:
        PushResponse result = wikiLocal.push(false, false)

        then:
        noExceptionThrown()
        !result.isSuccessful()
    }

    def "setOrigin"() {

        given:
        Repository repository = Mock(Repository)
        StoredConfig config = Mock(StoredConfig)
        gitLocal.getProjectName() >> 'x'
        gitLocal.projectDirParent >> temp
        gitLocal.taggerEmail >> '?'
        gitLocal.taggerName >> '?'
        wikiLocal.prepare(gitRemote, gitLocal)

        when:
        wikiLocal.setOrigin()

        then:
        1 * gitRemote.wikiCloneUrl() >> 'wikiUrl'
        1 * mockGit.getRepository() >> repository
        1 * repository.getConfig() >> config
        1 * config.setString("remote", "origin", "url", 'wikiUrl')

        then:
        config.save()
    }


    def "setOrigin fails, throws GitLocalException"() {

        given:
        Repository repository = Mock(Repository)
        StoredConfig config = Mock(StoredConfig)
        wikiLocal.remote = gitRemote
        config.save() >> { throw new NullPointerException() }

        when:
        wikiLocal.setOrigin()

        then:
        thrown GitLocalException
    }

    def "prepare(remote) is unsupported"() {
        when:
        wikiLocal.prepare(gitRemote)

        then:
        thrown UnsupportedOperationException
    }

    def "clone uses base url "() {
        given:
        wikiLocal.localConfiguration.cloneFromRemote = true
        wikiLocal.localConfiguration.projectName = 'wiggly'
        wikiLocal.localConfiguration.projectDirParent = temp
        String baseUrl = 'repo/base/url'
        String wikiUrl = 'repo/base/url.wiki'
        gitRemote.repoBaselUrl() >> baseUrl
        gitRemote.wikiCloneUrl() >> wikiUrl
        gitLocal.remote = gitRemote
        wikiLocal.remote = gitRemote

        when:
        wikiLocal.cloneRemote()

        then:
        cloner.cloned
        cloner.localDir == new File(temp, 'wiggly')
        cloner.remoteUrl == wikiUrl

    }
}
