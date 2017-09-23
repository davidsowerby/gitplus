package uk.q3c.build.gitplus.gitplus

import com.google.common.collect.ImmutableList
import com.google.inject.Provider
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.GitPlusFactory
import uk.q3c.build.gitplus.local.*
import uk.q3c.build.gitplus.remote.*
import uk.q3c.build.gitplus.remote.bitbucket.BitBucketRemote
import uk.q3c.build.gitplus.remote.github.GitHubRemote
import uk.q3c.build.gitplus.util.PropertiesResolver

import static org.mockito.Mockito.*
import static uk.q3c.build.gitplus.remote.ServiceProvider.*

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
    PropertiesResolver propertiesResolver
    GitPlusConfiguration gitPlusConfiguration = new DefaultGitPlusConfiguration()
    UrlParser urlParser = Mock(UrlParser)


    def setup() {
        propertiesResolver = mock(PropertiesResolver)
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
        gitplus = new DefaultGitPlus(local, wikiLocal, gitRemoteProvider, propertiesResolver, urlParser, gitPlusConfiguration)
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

    def "create remote repo only, no local or wiki"() {
        given:
        defaultProject()
        local.active >> false
        wikiLocal.active >> false
        gitHubRemote.active >> true
        gitHubRemote.create >> true


        when:
        gitplus.execute()

        then:
        0 * local.cloneRemote()
        0 * wikiLocal.cloneRemote()
        1 * gitHubRemote.createRepo()
        0 * local.push(_, _)
    }

    def "execute fails, throws GitPlusException"() {
        given:
        defaultProject()
        local.active >> false
        wikiLocal.active >> false
        gitHubRemote.active >> true
        gitHubRemote.create >> true


        when:
        gitplus.execute()

        then:
        1 * gitHubRemote.createRepo() >> { throw new IOException() }
        thrown GitPlusException
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


    def "cloneFromRemote sets up configuration correctly"() {
        given:
        GitPlus gitplus = GitPlusFactory.instance
        String projectName = "wiggly"
        String remoteUser = "davidsowerby"
        boolean includeWiki = false

        when: "wiki not active, default cloneExistsResponse"
        gitplus.cloneFromRemote(temp, remoteUser, projectName, includeWiki)
        gitplus.evaluate()

        then:
        gitplus.local.active
        gitplus.local.projectName == projectName
        gitplus.local.projectDirParent == temp
        gitplus.local.projectDir() == new File(temp, projectName)
        gitplus.local.cloneExistsResponse == CloneExistsResponse.EXCEPTION
        gitplus.remote.repoName == projectName
        gitplus.remote.repoUser == remoteUser
        !gitplus.wikiLocal.active

        when: "wiki active, changed cloneExistsResponse"
        includeWiki = true
        gitplus.cloneFromRemote(temp, remoteUser, projectName, includeWiki, CloneExistsResponse.DELETE)
        gitplus.evaluate()


        then:
        gitplus.local.active
        gitplus.local.projectName == projectName
        gitplus.local.projectDirParent == temp
        gitplus.local.projectDir() == new File(temp, projectName)
        gitplus.local.cloneExistsResponse == CloneExistsResponse.DELETE
        gitplus.remote.active
        gitplus.remote.repoName == projectName
        gitplus.remote.repoUser == remoteUser
        gitplus.wikiLocal.active
        gitplus.wikiLocal.projectName == projectName + ".wiki"
        gitplus.wikiLocal.projectDirParent == temp
        gitplus.wikiLocal.projectDir() == new File(temp, projectName + ".wiki")
        gitplus.wikiLocal.cloneExistsResponse == CloneExistsResponse.DELETE
    }

    def "createLocalAndRemote sets up configuration correctly"() {
        given:
        GitPlus gitplus = GitPlusFactory.instance
        String projectName = "wiggly"
        String remoteUser = "davidsowerby"
        boolean includeWiki = false
        ProjectCreator otherCreator = Mock(ProjectCreator)

        when: "default project creator, no wiki"
        gitplus.createLocalAndRemote(temp, remoteUser, projectName, includeWiki, true)
        gitplus.evaluate()

        then:
        gitplus.local.active
        gitplus.local.create
        gitplus.local.projectName == projectName
        gitplus.local.projectDirParent == temp
        gitplus.local.projectDir() == new File(temp, projectName)
        !gitplus.wikiLocal.active
        gitplus.remote.active
        gitplus.remote.repoName == projectName
        gitplus.remote.repoUser == remoteUser
        gitplus.remote.publicProject
        !gitplus.wikiLocal.active

        when: "other project creator, wiki included"
        includeWiki = true
        gitplus.createLocalAndRemote(temp, remoteUser, projectName, includeWiki, false, otherCreator)
        gitplus.evaluate()

        then:
        gitplus.local.active
        gitplus.local.projectName == projectName
        gitplus.local.projectDirParent == temp
        gitplus.local.projectDir() == new File(temp, projectName)
        gitplus.local.projectCreator == otherCreator
        gitplus.remote.active
        gitplus.remote.repoName == projectName
        gitplus.remote.repoUser == remoteUser
        !gitplus.remote.publicProject
        gitplus.wikiLocal.active
        gitplus.wikiLocal.projectName == projectName + ".wiki"
        gitplus.wikiLocal.projectDirParent == temp
        gitplus.wikiLocal.projectDir() == new File(temp, projectName + ".wiki")

    }

    def "useRemoteOnly"() {
        given:
        GitPlus gitplus = GitPlusFactory.instance
        String projectName = "wiggly"
        String remoteUser = "davidsowerby"

        when:
        gitplus.useRemoteOnly(remoteUser, projectName)
        gitplus.evaluate()

        then:
        !gitplus.local.active
        gitplus.remote.active
        gitplus.remote.repoName == projectName
        gitplus.remote.repoUser == remoteUser
        !gitplus.wikiLocal.active

    }

    def "createRemoteOnly"() {
        given:
        GitPlus gitplus = GitPlusFactory.instance
        String projectName = "wiggly"
        String remoteUser = "davidsowerby"

        when:
        gitplus.createRemoteOnly(remoteUser, projectName, true)
        gitplus.evaluate()

        then:
        !gitplus.local.active
        gitplus.remote.active
        gitplus.remote.publicProject
        gitplus.remote.repoName == projectName
        gitplus.remote.repoUser == remoteUser
        !gitplus.wikiLocal.active
    }


    def "close calls gitLocal.close"() {
        when:
        gitplus.close()

        then:
        1 * local.close()
        1 * wikiLocal.close()
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
