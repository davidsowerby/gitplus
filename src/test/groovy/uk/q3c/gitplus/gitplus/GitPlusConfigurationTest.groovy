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

        when:
        GitPlusConfiguration newConfig = new GitPlusConfiguration(config)

        then:
        newConfig.equals(config)
    }

    def "equals and hashcode same instance"() {
        given:
        GitPlusConfiguration configuration1 = new GitPlusConfiguration()
        GitPlusConfiguration configuration2 = configuration1

        expect:
        configuration1.equals(configuration2)
        configuration1.hashCode() == configuration2.hashCode()
    }

    def "equals and hashcode not equal null"() {
        given:
        GitPlusConfiguration configuration1 = new GitPlusConfiguration()
        GitPlusConfiguration configuration2 = null

        expect:
        !configuration1.equals(configuration2)
    }

    def "equals and hashcode differing elements"() {
        given:
        GitPlusConfiguration configuration1 = new GitPlusConfiguration()
        GitPlusConfiguration configuration2 = new GitPlusConfiguration()
        GitRemoteFactory remoteFactory1 = Mock(GitRemoteFactory)
        GitRemoteFactory remoteFactory2 = Mock(GitRemoteFactory)
        ProjectCreator projectCreator1 = Mock(ProjectCreator)
        ProjectCreator projectCreator2 = Mock(ProjectCreator)
        File f1 = Mock(File)
        File f2 = Mock(File)

        when:
        configuration1.createRemoteRepo(true)
        configuration2.createRemoteRepo(false)

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.createRemoteRepo(false).cloneRemoteRepo(true)
        configuration2.createRemoteRepo(false).cloneRemoteRepo(false)

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.cloneRemoteRepo(true).confirmRemoteDelete('dd')
        configuration2.cloneRemoteRepo(true).confirmRemoteDelete('da')

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.confirmRemoteDelete('dd').createProject(true)
        configuration2.confirmRemoteDelete('dd').createProject(false)

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()


        when:
        configuration1.createProject(true).gitRemoteFactory(remoteFactory1)
        configuration2.createProject(true).gitRemoteFactory(remoteFactory2)

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.gitRemoteFactory(remoteFactory1).projectCreator(projectCreator1)
        configuration2.gitRemoteFactory(remoteFactory1).projectCreator(projectCreator2)

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.projectCreator(projectCreator1).createLocalRepo(true)
        configuration2.projectCreator(projectCreator1).createLocalRepo(false)

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.createLocalRepo(true).publicProject(true)
        configuration2.createLocalRepo(true).publicProject(false)

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.publicProject(true).useWiki(true)
        configuration2.publicProject(true).useWiki(false)
        configuration1.isUseWiki()
        !configuration2.isUseWiki()

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.useWiki(true).remoteRepoHtmlUrl('a')
        configuration2.useWiki(true).remoteRepoHtmlUrl('b')

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.remoteRepoHtmlUrl('a').projectDescription('a')
        configuration2.remoteRepoHtmlUrl('a').projectDescription('b')

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.projectDescription('a').projectHomePage('a')
        configuration2.projectDescription('a').projectHomePage('b')

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.projectHomePage('a').projectDirParent(f1)
        configuration2.projectHomePage('a').projectDirParent(f2)

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.projectDirParent(f1).remoteRepoFullName('a')
        configuration2.projectDirParent(f1).remoteRepoFullName('b')

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()
        !configuration1.getRemoteRepoFullName().equals(configuration2.getRemoteRepoFullName())

        when:
        configuration1.remoteRepoFullName('a').projectName('a')
        configuration2.remoteRepoFullName('a').projectName('b')

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.projectName('a').projectDir(f1)
        configuration2.projectName('a').projectDir(f2)

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

        when:
        configuration1.projectDir(f1).remoteServiceProvider(GITHUB)
        configuration2.projectDir(f1).remoteServiceProvider(null)

        then:
        !configuration1.equals(configuration2)
        configuration1.hashCode() != configuration2.hashCode()

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

        when:
        config.repoDeleteApprover(approver).taggerName(taggerName).taggerEmail(taggerEmail).cloneUrl(cloneUrl)


        then:
        config.getRepoDeleteApprover() == approver
        config.getTaggerName().equals(taggerName)
        config.getTaggerEmail().equals(taggerEmail)
        config.getCloneUrl().equals(cloneUrl)
    }


}