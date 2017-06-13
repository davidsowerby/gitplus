package uk.q3c.build.gitplus.gitplus

import com.google.common.collect.ImmutableList
import com.google.inject.Provider
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.local.GitLocalException
import uk.q3c.build.gitplus.local.WikiLocal
import uk.q3c.build.gitplus.remote.*
import uk.q3c.build.gitplus.remote.bitbucket.BitBucketRemote
import uk.q3c.build.gitplus.remote.github.GitHubRemote

import static uk.q3c.build.gitplus.remote.ServiceProvider.BITBUCKET
import static uk.q3c.build.gitplus.remote.ServiceProvider.GITHUB
/**
 * Created by David Sowerby on 13 Mar 2016
 */
class DefaultGitPlusTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    File temp

    DefaultGitPlus gitplus

    GitHubRemote gitHubRemote = Mock(GitHubRemote)
    Provider<GitHubRemote> gitHubProvider = Mock(Provider)

    BitBucketRemote bitBucketRemote = Mock(BitBucketRemote)
    Provider<BitBucketRemote> bitBucketProvider = Mock(Provider)

    GitRemoteConfiguration gitRemoteConfiguration

    Map<ServiceProvider, Provider<GitRemote>> serviceProviders
    GitRemoteResolver gitRemoteProvider
    GitLocal local = Mock(GitLocal)
    WikiLocal wikiLocal = Mock(WikiLocal)
    final String projectName = "scratch"
    File projDirParent


    def setup() {
        gitRemoteConfiguration = new DefaultGitRemoteConfiguration()
        gitHubProvider.get() >> gitHubRemote
        gitHubRemote.getConfiguration() >> gitRemoteConfiguration
        bitBucketProvider.get() >> bitBucketRemote
        bitBucketRemote.getConfiguration() >> gitRemoteConfiguration
        serviceProviders = new HashMap<>()
        serviceProviders.put(GITHUB, gitHubProvider)
        serviceProviders.put(BITBUCKET, bitBucketProvider)
        temp = temporaryFolder.getRoot()
        gitRemoteProvider = new DefaultGitRemoteResolver(serviceProviders)
        gitplus = new DefaultGitPlus(local, wikiLocal, gitRemoteProvider)
        projDirParent = temp
    }


    def "create local repo but do not create project"() {
        given:
        defaultProject()
        local.getCreate() >> true
        wikiLocal.getCreate() >> false

        when:
        gitplus.execute()

        then:
        1 * local.createAndInitialise()
        0 * gitHubRemote.createRepo()
        0 * local.push(_, _)
    }


    def "clone remote repo, no wiki"() {
        given:
        defaultProject()
        local.getCreate() >> true
        local.getCloneFromRemote() >> true
        wikiLocal.getCloneFromRemote() >> false

        when:
        gitplus.execute()

        then:
        1 * local.cloneRemote()
        0 * wikiLocal.cloneRemote()
        0 * gitHubRemote.createRepo()
        0 * local.push(_, _)
    }

    def "clone remote repo, with wiki"() {
        given:
        defaultProject()
        local.getCreate() >> true
        local.getCloneFromRemote() >> true
        wikiLocal.getCloneFromRemote() >> true
        wikiLocal.getActive() >> true

        when:
        gitplus.execute()

        then:
        1 * local.cloneRemote()
        1 * wikiLocal.cloneRemote()
        0 * gitHubRemote.createRepo()
        0 * local.push(_, _)
    }

    /**
     * This does assume that the wiki repo is created remotely by the service provider - certainly true for GitHub, others may be different
     */
    def "using wiki, create local and remote repo, also clones wiki after create"() {
        given:
        defaultProject()
        local.getCreate() >> true
        gitHubRemote.getCreate() >> true
        wikiLocal.getCreate() >> true
        wikiLocal.getActive() >> true

        when:
        gitplus.execute()

        then:
        1 * local.createAndInitialise()
        1 * local.commit("Initial commit")
        1 * gitHubRemote.createRepo()
        1 * local.push(false, false)
        1 * wikiLocal.createAndInitialise()
        1 * wikiLocal.setOrigin()
    }


    def "clone and create false for local, verify remote origin from existing local"() {
        given:
        defaultProject()

        when:
        gitplus.execute()

        then:
        1 * local.verifyRemoteFromLocal()
    }


    def "close calls gitLocal.close"() {
        when:
        gitplus.close()

        then:
        1 * local.close()
        1 * wikiLocal.close()
    }

    def "GitPlusException when createOrVerify fails"() {
        given:
        local.verifyRemoteFromLocal() >> { throw new GitLocalException("msg") }

        when:
        gitplus.execute()

        then:
        thrown GitPlusException
    }

    /**
     * All this tests is that DefaultGitLocal is called to provide the commits
     * @return
     */
    def "extract develop and master commits"() {
        given:
        final String repoFullName = 'davidsowerby/scratch'

        List<GitCommit> expectedDevelopCommits = Mock(ImmutableList)
        List<GitCommit> expectedMasterCommits = Mock(ImmutableList)

        when:
        List<GitCommit> developCommits = gitplus.local.extractDevelopCommits()
        List<GitCommit> masterCommits = gitplus.local.extractMasterCommits()

        then:
        1 * local.extractDevelopCommits() >> expectedDevelopCommits
        1 * local.extractMasterCommits() >> expectedMasterCommits
        developCommits == expectedDevelopCommits
        masterCommits == expectedMasterCommits
    }

    def "change service provider"() {

        when:
        gitplus.setServiceProvider(BITBUCKET)

        then:
        gitplus.getServiceProvider() == BITBUCKET
    }

    def "getters"() {

        expect:
        gitplus.getRemoteResolver() == gitRemoteProvider
    }


    private void defaultProject() {
        local.getProjectName() >> projectName
        local.getProjectDirParent() >> projDirParent
    }
}
