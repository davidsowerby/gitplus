package uk.q3c.build.gitplus.gitplus

import com.google.common.collect.ImmutableList
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.changelog.ChangeLog
import uk.q3c.build.gitplus.changelog.ChangeLogConfiguration
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.local.GitLocalException
import uk.q3c.build.gitplus.local.GitLocalProvider
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.GitRemoteFactory

import static uk.q3c.build.gitplus.changelog.ChangeLogConfiguration.OutputTarget.PROJECT_BUILD_ROOT
import static uk.q3c.build.gitplus.changelog.ChangeLogConfiguration.OutputTarget.USE_FILE_SPEC

/**
 * Created by David Sowerby on 13 Mar 2016
 */
class GitPlusTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder();
    File temp

    GitPlus gitplus
    ProjectCreator projectCreator = Mock(ProjectCreator)
    GitRemoteFactory gitRemoteFactory = Mock(GitRemoteFactory)
    GitRemote gitRemote = Mock(GitRemote)
    GitLocal gitLocal = Mock(GitLocal)
    GitLocal wikiLocal = Mock(GitLocal)
    GitLocalProvider gitLocalProvider = Mock(GitLocalProvider)


    def setup() {
        gitRemoteFactory.createRemoteInstance(_) >> gitRemote
        gitLocalProvider.get(_) >>> [gitLocal, wikiLocal]
        temp = temporaryFolder.getRoot()
        gitplus = new GitPlus()
        gitplus.getConfiguration().gitLocalProvider(gitLocalProvider)
    }


    def "create local repo but do not create project"() {
        given:
        final String projectName = "scratch"
        gitplus.getConfiguration().createLocalRepo(true)
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
                .useWiki(false)


        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.createLocalRepo()
        0 * gitRemote.createRepo()
        0 * projectCreator.execute()
    }

    def "create local repo only and create project"() {
        given:
        final String projectName = "scratch"
        gitplus.getConfiguration().createLocalRepo(true)
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .projectCreator(projectCreator)
                .createLocalRepo(true)
                .createProject(true)
                .useWiki(false)

        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.createLocalRepo()
        1 * projectCreator.execute()
        0 * gitRemote.createRepo()
        0 * gitLocal.push(gitRemote, true)

    }

    def "clone remote repo"() {
        given:
        final String remoteRepoName = "davidsowerby/scratch"
        gitplus.getConfiguration().createLocalRepo(true)
                .remoteRepoFullName(remoteRepoName)
                .projectDirParent(temporaryFolder.getRoot())
                .projectCreator(projectCreator)
                .createLocalRepo(true)
                .createProject(true)
                .cloneRemoteRepo(true)


        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.cloneRemote()
        1 * wikiLocal.cloneRemote()
        0 * projectCreator.execute()
        0 * gitRemote.createRepo()
        0 * gitLocal.push(gitRemote, true)
    }


    def "create local and remote repo and create project"() {

        given:
        final String remoteRepoName = "davidsowerby/scratch"
        gitplus.getConfiguration()
                .createLocalRepo(true)
                .remoteRepoFullName(remoteRepoName)
                .projectDirParent(temporaryFolder.getRoot())
                .createLocalRepo(true)
                .createRemoteRepo(true)
                .gitRemoteFactory(gitRemoteFactory)
                .projectCreator(projectCreator)
                .createProject(true)
                .useWiki(false)

        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.createLocalRepo()
        1 * projectCreator.execute()
        1 * gitRemote.createRepo()
        2 * gitLocal.push(gitRemote, false)
    }

    /**
     * This does assume that the wiki repo is created remotely by the service provider - certainly true for GitHub, others may be different
     */
    def "using wiki, create local and remote repo, also clones wiki after create"() {
        given:
        final String remoteRepoName = "davidsowerby/scratch"
        gitplus.getConfiguration()
                .createLocalRepo(true)
                .remoteRepoFullName(remoteRepoName)
                .projectDirParent(temporaryFolder.getRoot())
                .createLocalRepo(true)
                .createRemoteRepo(true)
                .gitRemoteFactory(gitRemoteFactory)
                .projectCreator(projectCreator)
                .createProject(true)

        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.createLocalRepo()
        1 * projectCreator.execute()
        1 * gitRemote.createRepo()
        2 * gitLocal.push(gitRemote, false)
        1 * wikiLocal.createLocalRepo()
        1 * wikiLocal.setOrigin()
    }


    def "create local and remote repo but do not create project"() {
        given:
        final String remoteRepoName = "davidsowerby/scratch"
        gitplus.getConfiguration()
                .createLocalRepo(true)
                .remoteRepoFullName(remoteRepoName)
                .projectDirParent(temporaryFolder.getRoot())
                .createLocalRepo(true)
                .createRemoteRepo(true)
                .gitRemoteFactory(gitRemoteFactory)
                .useWiki(false)
        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.createLocalRepo()
        0 * projectCreator.execute()
        1 * gitRemote.createRepo()
        2 * gitLocal.push(gitRemote, false)
    }


    def "verify origin from existing local, origin has not been set"() {
        given:
        final String projectName = "scratch"
        gitplus.getConfiguration()
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
                .useWiki(false)

        when:
        gitplus.createOrVerifyRepos()

        then:
        1 * gitLocal.getOrigin() >> null
        gitplus.getConfiguration().getRemoteRepoHtmlUrl() == null
    }


    def "verify origin from existing local, origin has been set"() {
        given:

        final String projectName = "scratch"
        final String cloneUrl = 'https://github.com/davidsowerby/scratch.git'
        final String htmlUrl = 'https://github.com/davidsowerby/scratch'
        final String fullRepoName = 'davidsowerby/scratch'
        gitplus.getConfiguration()
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
                .useWiki(false)

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
        gitplus.getConfiguration()
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
                .useWiki(false)

        when:
        gitplus.getGitLocal()  // force creation to check closure
        gitplus.getWikiLocal() // force creation to check closure
        gitplus.close()

        then:
        1 * gitLocal.close()
        1 * wikiLocal.close()
    }

    def "GitPlusException when createOrVerify fails"() {
        given:
        gitLocal.getOrigin() >> { throw new GitLocalException("msg") }
        final String projectName = "scratch"
        gitplus.getConfiguration()
                .projectName(projectName)
                .projectDirParent(temporaryFolder.getRoot())
                .gitRemoteFactory(gitRemoteFactory)
                .useWiki(false)

        when:
        gitplus.createOrVerifyRepos()

        then:
        thrown GitPlusException
    }

    def "configuration elements returned"() {
        given:
        final String projectName = 'krail'
        final File projectDir = new File('user/home/git/project')
        gitplus.getConfiguration()
                .projectName(projectName)
                .projectDir(projectDir)

        expect:
        gitplus.getProjectName().equals(projectName)
        gitplus.getProjectDir().equals(projectDir)

    }

    def "getLocalRepo"() {
        given:
        final String repoFullName = 'davidsowerby/scratch'
        Git git = Mock(Git)
        Repository repo = Mock(Repository)
        gitplus.getConfiguration().remoteRepoFullName(repoFullName)

        when:
        gitplus.getLocalRepo()

        then:
        1 * gitLocal.getGit() >> git
        1 * git.getRepository() >> repo

    }

    def "get tags"() {
        given:
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
        gitplus.getConfiguration().remoteRepoFullName(repoFullName)

        List<GitCommit> expectedDevelopCommits = Mock(ImmutableList)
        List<GitCommit> expectedMasterCommits = Mock(ImmutableList)

        when:
        List<GitCommit> developCommits = gitplus.extractDevelopCommits()
        List<GitCommit> masterCommits = gitplus.extractMasterCommits()

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
        gitplus.getConfiguration()
                .remoteRepoFullName(repoFullName)
                .gitRemoteFactory(gitRemoteFactory)
                .projectName('scratch')

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
        gitplus.getConfiguration()
                .remoteRepoFullName(repoFullName)
                .gitRemoteFactory(gitRemoteFactory)
                .projectDir(new File(temp, 'scratch'))
        gitRemoteFactory.createRemoteInstance(_) >> gitRemote

        when:
        gitplus.push(false)

        then:
        1 * gitLocal.push(gitRemote, false)
    }

    def "push wiki"() {
        given:
        final String repoFullName = 'davidsowerby/scratch'

        gitplus.getConfiguration()
                .remoteRepoFullName(repoFullName)
                .gitRemoteFactory(gitRemoteFactory)
                .projectDir(new File(temp, 'scratch'))
        gitRemoteFactory.createRemoteInstance(_) >> gitRemote
        gitplus.getGitLocal() // fudge to make GitLocalProvider return sequence correct

        when:

        gitplus.pushWiki()

        then:
        1 * wikiLocal.push(gitRemote, false)
    }

    def "get Change Log"() {
        expect:
        gitplus.getChangelog() instanceof ChangeLog
    }

    def "generate change log, config already set"() {
        given:
        ChangeLog changeLog = Mock(ChangeLog)
        gitplus.setChangelog(changeLog)
        File f = new File('.')

        when:
        File result = gitplus.generateChangeLog()

        then:
        1 * changeLog.createChangeLog() >> f
        result == f
    }

    def "generate change log, config provided"() {
        given:
        ChangeLog changeLog = Mock(ChangeLog)
        gitplus.setChangelog(changeLog)
        File f = new File('.')
        ChangeLogConfiguration changeLogConfiguration = Mock(ChangeLogConfiguration)

        when:
        File result = gitplus.generateChangeLog(changeLogConfiguration)

        then:
        1 * changeLog.setConfiguration(changeLogConfiguration)
        1 * changeLog.createChangeLog() >> f
        result == f
    }

    def "generate change log, change output target"() {
        given:
        ChangeLog changeLog = Mock(ChangeLog)
        gitplus.setChangelog(changeLog)
        File f = new File('.')
        ChangeLogConfiguration changeLogConfiguration = Mock(ChangeLogConfiguration)

        when:
        File result = gitplus.generateChangeLog(PROJECT_BUILD_ROOT)

        then:
        1 * changeLog.getConfiguration() >> changeLogConfiguration
        1 * changeLogConfiguration.outputTarget(PROJECT_BUILD_ROOT)
        1 * changeLog.createChangeLog() >> f
        result == f
    }

    def "generate change log, change output file"() {
        given:
        ChangeLog changeLog = Mock(ChangeLog)
        gitplus.setChangelog(changeLog)
        File f = new File('.')
        ChangeLogConfiguration changeLogConfiguration = Mock(ChangeLogConfiguration)

        when:
        File result = gitplus.generateChangeLog(f)

        then:
        1 * changeLog.getConfiguration() >> changeLogConfiguration
        1 * changeLogConfiguration.outputFile(f) >> changeLogConfiguration
        1 * changeLogConfiguration.outputTarget(USE_FILE_SPEC) >> changeLogConfiguration
        1 * changeLog.createChangeLog() >> f
        result == f
    }

}
