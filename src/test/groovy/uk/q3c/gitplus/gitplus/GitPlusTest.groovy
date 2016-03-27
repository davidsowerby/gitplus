package uk.q3c.gitplus.gitplus

import com.google.common.collect.ImmutableSet
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.gitplus.local.GitLocal
import uk.q3c.gitplus.local.GitLocalException
import uk.q3c.gitplus.remote.GitRemote
import uk.q3c.gitplus.remote.GitRemoteFactory

import static uk.q3c.gitplus.remote.GitRemote.ServiceProvider.GITHUB

/**
 * Created by David Sowerby on 13 Mar 2016
 */
class GitPlusTest extends Specification {

    final String remoteScratchUrl = "https://github.com/davidsowerby/scratch"

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder();

    GitPlus gitplus
    ProjectCreator projectCreator = Mock(ProjectCreator)
    GitRemoteFactory gitRemoteFactory = Mock(GitRemoteFactory)
    GitRemote gitRemote = Mock(GitRemote)
    GitLocal gitLocal = Mock(GitLocal)
    GitPlusConfiguration configuration

    def setup() {
        gitRemoteFactory.createRemoteInstance(_) >> gitRemote
        configuration = new GitPlusConfiguration()
    }


    def "create local repo but do not create project"() {
        given:
        final String projectName = "scratch"
        GitPlusConfiguration configuration = new GitPlusConfiguration().createLocalRepo(true)
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
        gitplus = new GitPlus(configuration, gitLocal)

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
        gitplus = new GitPlus(configuration, gitLocal)

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



        gitplus = new GitPlus(configuration, gitLocal)

        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.cloneRemote()
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

        when:
        gitplus = new GitPlus(configuration, gitLocal)
        gitplus.createOrVerifyRepos()

        then:
        1 * gitRemoteFactory.projectNameFromRemoteRepFullName(GITHUB, remoteRepoName) >> 'scratch'
        1 * gitLocal.createLocalRepo()
        1 * projectCreator.execute(new File(temporaryFolder.getRoot(), "scratch"))
        1 * gitRemote.createRepo()
        2 * gitLocal.push(gitRemote, false)
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

        gitplus = new GitPlus(configuration, gitLocal)
        gitplus.createOrVerifyRepos()

        then:
        1 * gitRemoteFactory.projectNameFromRemoteRepFullName(GITHUB, remoteRepoName) >> 'scratch'
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

        gitplus = new GitPlus(configuration, gitLocal)

        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.getOrigin() >> null
        configuration.getRemoteRepoUrl() == null
    }

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

        gitplus = new GitPlus(configuration, gitLocal)


        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.getOrigin() >> cloneUrl
        1 * gitRemoteFactory.repoFullNameFromCloneUrl(GITHUB, cloneUrl) >> fullRepoName
        configuration.getRemoteRepoFullName().equals(fullRepoName)

    }


    def "close calls gitLocal.close"() {
        given:
        final String projectName = "scratch"
        GitPlusConfiguration configuration = new GitPlusConfiguration()
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
                .apiToken("token")

        gitplus = new GitPlus(configuration, gitLocal)


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

        gitplus = new GitPlus(configuration, gitLocal)


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

        gitplus = new GitPlus(configuration, gitLocal)

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
        gitplus = new GitPlus(configuration, gitLocal)

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
        gitplus = new GitPlus(configuration, gitLocal)

        List<Ref> expectedTags = Mock(List)

        when:
        List<Ref> tags = gitplus.getTags()

        then:
        1 * gitLocal.getTags() >> expectedTags
        tags == expectedTags
    }

    def "extract develop and master commits"() {
        given:
        final String repoFullName = 'davidsowerby/scratch'
        Git git = Mock(Git)
        Repository repo = Mock(Repository)

        configuration.remoteRepoFullName(repoFullName)
        gitplus = new GitPlus(configuration, gitLocal)

        Set<RevCommit> expectedDevelopCommits = Mock(ImmutableSet)
        Set<RevCommit> expectedMasterCommits = Mock(ImmutableSet)

        when:
        Set<RevCommit> developCommits = gitplus.extractDevelopCommits()
        Set<RevCommit> masterCommits = gitplus.extractMasterCommits()

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
                .remoteRepoFullName('davidsowerby/scratch')
                .projectName('scratch')
        gitplus = new GitPlus(configuration, gitLocal)

        when:
        String tagUrl = gitplus.getRemoteTagUrl()
        String htmlUrl = gitplus.getRemoteHtmlUrl()

        then:
        1 * gitRemote.getTagUrl() >> expectedTagUrl
        1 * gitRemote.getHtmlUrl() >> expectedHtmlUrl
        tagUrl == expectedTagUrl
        htmlUrl == expectedHtmlUrl
    }


}
