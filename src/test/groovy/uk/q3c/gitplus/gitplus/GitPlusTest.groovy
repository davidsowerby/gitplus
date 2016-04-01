package uk.q3c.gitplus.gitplus

import com.google.common.collect.ImmutableSet
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.gitplus.local.GitCommit
import uk.q3c.gitplus.local.GitLocal
import uk.q3c.gitplus.local.GitLocalException
import uk.q3c.gitplus.local.GitLocalProvider
import uk.q3c.gitplus.remote.GitRemote
import uk.q3c.gitplus.remote.GitRemoteFactory

/**
 * Created by David Sowerby on 13 Mar 2016
 */
class GitPlusTest extends Specification {

    final String remoteScratchUrl = "https://github.com/davidsowerby/scratch"

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder();
    File temp

    GitPlus gitplus
    ProjectCreator projectCreator = Mock(ProjectCreator)
    GitRemoteFactory gitRemoteFactory = Mock(GitRemoteFactory)
    GitRemote gitRemote = Mock(GitRemote)
    GitLocal gitLocal = Mock(GitLocal)
    GitPlusConfiguration configuration
    GitLocal wikiLocal = Mock(GitLocal)
    GitLocalProvider gitLocalProvider = Mock(GitLocalProvider)


    def setup() {
        gitRemoteFactory.createRemoteInstance(_) >> gitRemote
        configuration = new GitPlusConfiguration()
        gitLocalProvider.get(_) >>> [gitLocal, wikiLocal]
        temp = temporaryFolder.getRoot()
    }


    def "create local repo but do not create project"() {
        given:
        final String projectName = "scratch"
        GitPlusConfiguration configuration = new GitPlusConfiguration().createLocalRepo(true)
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
                .useWiki(false)
        gitplus = new GitPlus(configuration, gitLocalProvider)

        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.createLocalRepo()
        0 * gitRemote.createRepo()
        0 * projectCreator.execute(configuration.getProjectDir())
    }

    def "create local repo only and create project"() {
        given:
        final String projectName = "scratch"
        GitPlusConfiguration configuration = new GitPlusConfiguration().createLocalRepo(true)
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .projectCreator(projectCreator)
                .createLocalRepo(true)
                .createProject(true)
                .useWiki(false)
        gitplus = new GitPlus(configuration, gitLocalProvider)

        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.createLocalRepo()
        1 * projectCreator.execute(configuration.getProjectDir())
        0 * gitRemote.createRepo()
        0 * gitLocal.push(gitRemote, true)

    }

    def "clone remote repo"() {
        given:
        final String remoteRepoName = "davidsowerby/scratch"
        GitPlusConfiguration configuration = new GitPlusConfiguration().createLocalRepo(true)
                .remoteRepoFullName(remoteRepoName)
                .projectDirParent(temporaryFolder.getRoot())
                .projectCreator(projectCreator)
                .createLocalRepo(true)
                .createProject(true)
                .cloneRemoteRepo(true)
                .apiToken("token")



        gitplus = new GitPlus(configuration, gitLocalProvider)

        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.cloneRemote()
        1 * wikiLocal.cloneRemote()
        0 * projectCreator.execute(configuration.getProjectDir())
        0 * gitRemote.createRepo()
        0 * gitLocal.push(gitRemote, true)
    }


    def "create local and remote repo and create project"() {

        given:
        final String remoteRepoName = "davidsowerby/scratch"
        configuration = new GitPlusConfiguration()
        configuration
                .createLocalRepo(true)
                .remoteRepoFullName(remoteRepoName)
                .projectDirParent(temporaryFolder.getRoot())
                .createLocalRepo(true)
                .createRemoteRepo(true)
                .gitRemoteFactory(gitRemoteFactory)
                .createProject(true)
                .projectCreator(projectCreator)
                .apiToken("token")
                .useWiki(false)

        when:
        gitplus = new GitPlus(configuration, gitLocalProvider)
        gitplus.createOrVerifyRepos()

        then:
        1 * gitRemoteFactory.projectNameFromFullRepoName(remoteRepoName) >> 'scratch'
        1 * gitLocal.createLocalRepo()
        1 * projectCreator.execute(new File(temporaryFolder.getRoot(), "scratch"))
        1 * gitRemote.createRepo()
        2 * gitLocal.push(gitRemote, false)
    }

    /**
     * This does assume that the wiki repo is create remotely by the service provider - certianly true for GitHub, others may be different
     */
    def "using wiki, create local and remote repo, also clones wiki after create"() {
        given:
        final String remoteRepoName = "davidsowerby/scratch"
        configuration = new GitPlusConfiguration()
        configuration
                .createLocalRepo(true)
                .remoteRepoFullName(remoteRepoName)
                .projectDirParent(temporaryFolder.getRoot())
                .createLocalRepo(true)
                .createRemoteRepo(true)
                .gitRemoteFactory(gitRemoteFactory)
                .createProject(true)
                .projectCreator(projectCreator)
                .apiToken("token")

        when:
        gitplus = new GitPlus(configuration, gitLocalProvider)
        gitplus.createOrVerifyRepos()

        then:
        1 * gitRemoteFactory.projectNameFromFullRepoName(remoteRepoName) >> 'scratch'
        1 * gitLocal.createLocalRepo()
        1 * projectCreator.execute(new File(temporaryFolder.getRoot(), "scratch"))
        1 * gitRemote.createRepo()
        2 * gitLocal.push(gitRemote, false)
        1 * wikiLocal.createLocalRepo()
        1 * wikiLocal.setOrigin()
    }


    def "create local and remote repo but do not create project"() {
        given:
        GitPlusConfiguration configuration = new GitPlusConfiguration()
        final String remoteRepoName = "davidsowerby/scratch"

        when:
        configuration.createLocalRepo(true)
                .remoteRepoFullName(remoteRepoName)
                .projectDirParent(temporaryFolder.getRoot())
                .createLocalRepo(true)
                .createRemoteRepo(true)
                .gitRemoteFactory(gitRemoteFactory)
                .apiToken("token")
                .useWiki(false)

        gitplus = new GitPlus(configuration, gitLocalProvider)
        gitplus.createOrVerifyRepos()

        then:
        1 * gitRemoteFactory.projectNameFromFullRepoName(remoteRepoName) >> 'scratch'
        1 * gitLocal.createLocalRepo()
        0 * projectCreator.execute(configuration.getProjectDir())
        1 * gitRemote.createRepo()
        2 * gitLocal.push(gitRemote, false)
    }


    def "verify origin from existing local, origin has not been set"() {
        given:
        final String projectName = "scratch"
        GitPlusConfiguration configuration = new GitPlusConfiguration()
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
                .apiToken("token")
                .useWiki(false)

        gitplus = new GitPlus(configuration, gitLocalProvider)

        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.getOrigin() >> null
        configuration.getRemoteRepoHtmlUrl() == null
    }

    /**
     * Config is cloned on construction, so call GitPlus.getConfiguration() to see the changes
     * @return
     */
    def "verify origin from existing local, origin has been set"() {
        given:

        final String projectName = "scratch"
        final String cloneUrl = 'https://github.com/davidsowerby/scratch.git'
        final String htmlUrl = 'https://github.com/davidsowerby/scratch'
        final String fullRepoName = 'davidsowerby/scratch'
        GitPlusConfiguration configuration = new GitPlusConfiguration()
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
                .apiToken("token")
                .useWiki(false)
        configuration.validate()
        gitplus = new GitPlus(configuration, gitLocalProvider)


        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.getOrigin() >> cloneUrl
        1 * gitRemoteFactory.fullRepoNameFromCloneUrl(cloneUrl) >> fullRepoName
        gitplus.getConfiguration().getRemoteRepoFullName().equals(fullRepoName)

    }


    def "close calls gitLocal.close"() {
        given:
        final String projectName = "scratch"
        GitPlusConfiguration configuration = new GitPlusConfiguration()
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
                .apiToken("token")
                .useWiki(false)

        gitplus = new GitPlus(configuration, gitLocalProvider)


        when:
        gitplus.close()

        then:
        1 * gitLocal.close()
    }

    def "GitPlusException when createOrVerify fails"() {
        given:
        gitLocal.getOrigin() >> { throw new GitLocalException("msg") }
        final String projectName = "scratch"
        GitPlusConfiguration configuration = new GitPlusConfiguration()
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
                .apiToken("token")
                .useWiki(false)

        gitplus = new GitPlus(configuration, gitLocalProvider)


        when:
        gitplus.createOrVerifyRepos()

        then:
        thrown GitPlusException
    }

    def "configuration elements returned"() {
        given:
        final String projectName = 'krail'
        final File projectDir = new File('user/home/git/project')
        GitPlusConfiguration configuration = new GitPlusConfiguration()
                .projectName(projectName)
                .projectDir(projectDir)


        gitplus = new GitPlus(configuration, gitLocalProvider)

        expect:
        gitplus.getProjectName().equals(projectName)
        gitplus.getProjectDir().equals(projectDir)

    }

    def "getLocalRepo"() {
        given:
        final String repoFullName = 'davidsowerby/scratch'
        Git git = Mock(Git)
        Repository repo = Mock(Repository)

        configuration.remoteRepoFullName(repoFullName)
        gitplus = new GitPlus(configuration, gitLocalProvider)

        when:
        gitplus.getLocalRepo()

        then:
        1 * gitLocal.getGit() >> git
        1 * git.getRepository() >> repo

    }

    def "get tags"() {
        given:
        final String repoFullName = 'davidsowerby/scratch'
        Git git = Mock(Git)
        Repository repo = Mock(Repository)

        configuration.remoteRepoFullName(repoFullName)
        gitplus = new GitPlus(configuration, gitLocalProvider)

        List<Ref> expectedTags = Mock(List)

        when:
        List<Ref> tags = gitplus.getTags()

        then:
        1 * gitLocal.getTags() >> expectedTags
        tags == expectedTags
    }

    /**
     * All this tests is that GitLocal is called to provide the commits
     * @return
     */
    def "extract develop and master commits"() {
        given:
        final String repoFullName = 'davidsowerby/scratch'
        Git git = Mock(Git)
        Repository repo = Mock(Repository)

        configuration.remoteRepoFullName(repoFullName)
        gitplus = new GitPlus(configuration, gitLocalProvider)

        Set<GitCommit> expectedDevelopCommits = Mock(ImmutableSet)
        Set<GitCommit> expectedMasterCommits = Mock(ImmutableSet)

        when:
        Set<GitCommit> developCommits = gitplus.extractDevelopCommits()
        Set<GitCommit> masterCommits = gitplus.extractMasterCommits()

        then:
        1 * gitLocal.extractDevelopCommits() >> expectedDevelopCommits
        1 * gitLocal.extractMasterCommits() >> expectedMasterCommits
        developCommits == expectedDevelopCommits
        masterCommits == expectedMasterCommits


    }

    def "tagUrl and htmlUrl"() {
        given:
        final String repoFullName = 'davidsowerby/scratch'
        final String expectedTagUrl = 'https://github.com/davidsowerby/scratch/tree'
        final String expectedHtmlUrl = 'https://github.com/davidsowerby/scratch'
        gitRemoteFactory.createRemoteInstance(_) >> gitRemote
        configuration.remoteRepoFullName(repoFullName)
                .gitRemoteFactory(gitRemoteFactory)
                .projectName('scratch')
        gitplus = new GitPlus(configuration, gitLocalProvider)

        when:
        String tagUrl = gitplus.getRemoteTagUrl()
        String htmlUrl = gitplus.getRemoteHtmlUrl()

        then:
        1 * gitRemote.getTagUrl() >> expectedTagUrl
        1 * gitRemote.getHtmlUrl() >> expectedHtmlUrl
        tagUrl == expectedTagUrl
        htmlUrl == expectedHtmlUrl
    }

    def "push"() {
        given:
        final String repoFullName = 'davidsowerby/scratch'
        configuration.remoteRepoFullName(repoFullName).gitRemoteFactory(gitRemoteFactory).projectDir(new File(temp, 'scratch'))
        gitRemoteFactory.createRemoteInstance(_) >> gitRemote
        gitplus = new GitPlus(configuration, gitLocalProvider)

        when:
        gitplus.push(false)

        then:
        1 * gitLocal.push(gitRemote, false)
    }

    def "push wiki"() {
        given:
        final String repoFullName = 'davidsowerby/scratch'
        configuration.remoteRepoFullName(repoFullName).gitRemoteFactory(gitRemoteFactory).projectDir(new File(temp, 'scratch'))
        gitRemoteFactory.createRemoteInstance(_) >> gitRemote
        gitplus = new GitPlus(configuration, gitLocalProvider)

        when:
        gitplus.pushWiki()

        then:
        1 * wikiLocal.push(gitRemote, false)
    }

}
