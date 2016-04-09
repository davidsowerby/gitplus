package uk.q3c.gitplus.gitplus

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.gitplus.remote.DefaultGitRemoteFactory
import uk.q3c.gitplus.remote.DefaultRemoteRepoDeleteApprover
import uk.q3c.gitplus.remote.GitRemoteFactory
import uk.q3c.gitplus.remote.RemoteRepoDeleteApprover
import uk.q3c.gitplus.util.BuildPropertiesLoader
import uk.q3c.gitplus.util.FileBuildPropertiesLoader

import static uk.q3c.gitplus.gitplus.GitPlusConfiguration.CloneExistsResponse.*
import static uk.q3c.gitplus.remote.GitRemote.ServiceProvider.BITBUCKET
import static uk.q3c.gitplus.remote.GitRemote.ServiceProvider.GITHUB

/**
 * Created by David Sowerby on 14 Mar 2016
 */
class GitPlusConfigurationTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    File temp

    GitPlusConfiguration config
    ProjectCreator projectCreator = Mock(ProjectCreator)
    GitRemoteFactory remoteFactory = Mock(GitRemoteFactory)

    def setup() {
        config = new GitPlusConfiguration()
        temp = temporaryFolder.getRoot()
    }

    def "defaults"() {
        expect:
        config.projectDir == null
        !config.createLocalRepo
        !config.cloneRemoteRepo
        !config.createProject
        config.projectCreator == null
        !config.isCreateRemoteRepo()
        !config.isPublicProject()
        config.getRemoteServiceProvider().equals(GITHUB)
        config.getPropertiesLoader() instanceof FileBuildPropertiesLoader
        config.getRepoDeleteApprover() instanceof DefaultRemoteRepoDeleteApprover
        config.getIssueLabels().equals(GitPlusConfiguration.defaultIssueLabels)
        !config.isMergeIssueLabels()
        config.getCloneExistsResponse() == EXCEPTION

    }

    def "creating a local repo throws exception if no project name"() {

        given:
        config.createLocalRepo(true)

        when:
        config.validate()

        then:
        thrown(GitPlusConfigurationException)
    }

    def "creating a remote repo throws exception if no remote repo url"() {

        given:
        config.createRemoteRepo(true)

        when:
        config.validate()

        then:
        thrown(GitPlusConfigurationException)
    }

    def "cloning a remote repo throws exception if no remote repo url"() {

        given:
        config.cloneRemoteRepo(true)

        when:
        config.validate()

        then:
        thrown(GitPlusConfigurationException)
    }

    def "cloning a remote repo throws exception if no apiToken"() {
        given:
        config.cloneRemoteRepo(true)

        when:
        config.validate()

        then:
        thrown(GitPlusConfigurationException)
    }

    def "creating a remote repo throws exception if no apiToken"() {
        given:
        config.createRemoteRepo(true)

        when:
        config.validate()

        then:
        thrown(GitPlusConfigurationException)
    }

    def "create local repo uses current directory if projectDirParent is null"() {
        given:
        config.createLocalRepo(true).projectName("scratch")

        when:
        config.validate()

        then:
        config.getProjectDirParent().equals(new File("."))
    }

    def "clone remote repo uses current directory if projectDirParent is null"() {
        given:
        config.cloneRemoteRepo(true).projectName("dummy").remoteRepoFullName('davidsowerby/scratch')

        when:
        config.validate()

        then:
        config.getProjectDirParent().equals(new File("."))
    }

    def "createLocal and cloneRemote reset each other"() {
        when:
        config.cloneRemoteRepo(true).createLocalRepo(true)

        then:
        !config.isCloneRemoteRepo()
        config.createLocalRepo

        when:
        config.createLocalRepo(true).cloneRemoteRepo(true)

        then:
        config.isCloneRemoteRepo()
        !config.createLocalRepo
    }

    def "if createProject true, exception thrown if projectCreator null"() {
        when:
        config.createProject(true).projectName("dummy")
        config.validate()


        then:
        thrown(GitPlusConfigurationException)

        when:
        config.projectCreator(projectCreator).projectName("dummy")
        config.validate()

        then:
        notThrown(GitPlusConfigurationException)
    }

    def "if projectDir not set, construct from localGitRoot and projectName"() {
        when:
        config.createLocalRepo(true).projectDirParent(temp).projectName("scratch")
        config.validate()

        then:
        config.getProjectDir().equals(new File(temp, "scratch"))
    }

    def "validate sets the remote factory provider"() {
        when:
        config.createLocalRepo(true).projectDirParent(temp).projectName("scratch")
        config.validate()

        then:
        config.getGitRemoteFactory().getRemoteServiceProvider() == GITHUB
    }

    def "validate throes exception if clone DELETE is set without safeApproval"() {
        given:
        FileDeleteApprover deleteApprover = Mock(FileDeleteApprover)
        config.cloneExistsResponse(DELETE)

        when:
        config.validate()

        then:
        thrown GitPlusConfigurationException

        when:
        config.fileDeleteApprover(deleteApprover)

        then:
        noExceptionThrown()
    }


    def "project description and homepage are data only"() {
        when:
        config.projectDescription("description").projectHomePage("homepage")

        then:
        config.getProjectDescription().equals("description")
        config.getProjectHomePage().equals("homepage")
    }


    def "projectName and projectDir null, throws exception"() {
        when:
        config.validate()

        then:
        thrown GitPlusConfigurationException
    }

    def "full repo url, validation has been done"() {
        given:
        config.remoteRepoFullName("davidsowerby/scratch").projectName('scratch')

        when:
        config.validate()

        then:
        config.getRemoteRepoHtmlUrl().equals("https://github.com/davidsowerby/scratch")
    }

    def "full repo url, no validation throws GitPlusConfigurationException"() {
        given:
        config.remoteRepoFullName("davidsowerby/scratch").projectName('scratch')

        when:
        config.getRemoteRepoHtmlUrl()

        then:

        thrown GitPlusConfigurationException
    }


    def "no remote factory specified, use default"() {
        given:
        config.projectName('scratch')

        when:
        config.validate()

        then:
        config.getGitRemoteFactory() instanceof DefaultGitRemoteFactory
    }

    def "null project name uses repo name if available"() {
        given:
        config.remoteRepoFullName('davidsowerby/scratch')

        expect:
        config.getProjectName().equals('scratch')
    }


    def "use project name if available, not repo name"() {
        given:
        config.remoteRepoFullName('davidsowerby/scratch')
        config.projectName('wiggly')

        expect:
        config.getProjectName().equals('wiggly')
    }

    def "create local repo with projectParentDir, projectParent defaults to current dir"() {
        given:
        config.createLocalRepo(true).projectName('dummy')

        when:
        config.validate()

        then:
        config.getProjectDirParent().equals(new File('.'))
    }

    def "copy constructor"() {
        given:
        BuildPropertiesLoader propertiesLoader = Mock(BuildPropertiesLoader)
        config.createLocalRepo(true)
                .confirmRemoteDelete('dd')
                .createProject(true)
                .useWiki(false)
                .createRemoteRepo(true)
                .projectCreator(projectCreator)
                .gitRemoteFactory(remoteFactory)
                .propertiesLoader(propertiesLoader)
                .remoteRepoName('dummy')

        when:
        GitPlusConfiguration newConfig = new GitPlusConfiguration(config)

        then:
        newConfig.getProjectDir().equals(config.getProjectDir())
        newConfig.getProjectName().equals(config.getProjectName())
        newConfig.getIssueLabels().equals(config.getIssueLabels())
        newConfig.getApiTokenCreateRepo().equals(config.getApiTokenCreateRepo())
        newConfig.getApiTokenDeleteRepo().equals(config.getApiTokenDeleteRepo())
        newConfig.getApiTokenRestricted().equals(config.getApiTokenRestricted())
        newConfig.getRemoteServiceProvider().equals(config.getRemoteServiceProvider())
    }


    def "getApiTokenRestricted"() {
        expect:
        config.getApiTokenRestricted() != null
        config.getApiTokenDeleteRepo() != null
        config.getApiTokenCreateRepo() != null
    }

    def "set and get"() {
        given:
        RemoteRepoDeleteApprover approver = Mock(RemoteRepoDeleteApprover)
        String taggerName = 'a'
        String taggerEmail = 'b'
        String cloneUrl = 'url'
        Map<String, String> labels = Mock(Map)
        File projectDir = new File('.')
        FileDeleteApprover deleteApprover = Mock()

        when:
        config.repoDeleteApprover(approver)
                .taggerName(taggerName)
                .taggerEmail(taggerEmail)
                .cloneUrl(cloneUrl)
                .issueLabels(labels)
                .mergeIssueLabels(true)
                .publicProject(true)
                .confirmRemoteDelete('whatever')
                .remoteServiceProvider(BITBUCKET)
                .remoteRepoHtmlUrl('thingy')
                .projectDir(projectDir)
                .useWiki(true)
                .cloneExistsResponse(PULL)
                .fileDeleteApprover(deleteApprover)


        then:
        config.getRepoDeleteApprover() == approver
        config.getTaggerName().equals(taggerName)
        config.getTaggerEmail().equals(taggerEmail)
        config.getCloneUrl().equals(cloneUrl)
        config.getIssueLabels() == labels
        config.isMergeIssueLabels()
        config.isPublicProject()
        config.getConfirmRemoteDelete().equals('whatever')
        config.getRemoteServiceProvider() == BITBUCKET
        config.getProjectDir().equals(projectDir)
        config.isUseWiki()
        config.getCloneExistsResponse() == PULL
        config.getFileDeleteApprover() == deleteApprover
    }

    def "force load from properties"() {
        expect:
        config.getTaggerName().equals('David Sowerby')
        config.getTaggerEmail().equals('david.sowerby@virgin.net')
    }

    def "project name not set and remoteRepoName also null"() {
        when:
        config.getProjectName()

        then:
        thrown GitPlusConfigurationException
    }

    def "set remoteRepoFullname with no '/'"() {
        when:
        config.remoteRepoFullName('wiggly')

        then:
        thrown GitPlusConfigurationException
    }

    def "confirm delete"() {
        given:
        config.remoteRepoFullName('davidsowerby/dummy').
                confirmRemoteDelete("I really, really want to delete the davidsowerby/dummy repo from GitHub")
        expect:
        config.deleteRepoApproved()
    }

    def "reject delete"() {
        given:
        config.remoteRepoFullName('davidsowerby/dummy').
                confirmRemoteDelete("I really really want to delete the davidsowerby/dummy repo from GitHub")
        expect:
        !config.deleteRepoApproved()
    }


    def "cloneUrl is null, get from htmlUrl"() {
        given:
        config.remoteRepoHtmlUrl('somewhere')

        expect:
        config.getCloneUrl().equals('somewhere.git')
    }

    def "getRemoteRepoFullName(), return null if either user or repoName null"() {
        when:
        String result = config.getRemoteRepoFullName()

        then:
        result == null

        when:
        config.remoteRepoName('repo')
        result = config.getRemoteRepoFullName()

        then:
        result == null

        when:
        config.remoteRepoName(null).remoteRepoUser('me')
        result = config.getRemoteRepoFullName()

        then:
        result == null

    }

    def "getRemoteRepoFullName() combines user and repoName"() {
        given:
        config.remoteRepoUser('me').remoteRepoName('repo')

        expect:
        config.getRemoteRepoFullName().equals('me/repo')
    }

    def "remoteRepoFullName(fullName) sets user and repoName"() {
        given:
        config.remoteRepoFullName('me/repo')

        expect:
        config.getRemoteRepoUser().equals('me')
        config.getRemoteRepoName().equals('repo')
        config.getRemoteRepoFullName().equals('me/repo')


    }
}