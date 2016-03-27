package uk.q3c.gitplus.gitplus

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.gitplus.remote.DefaultGitRemoteFactory

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

    def setup() {
        config = new GitPlusConfiguration()
        temp = temporaryFolder.getRoot()
    }

    def "defaults"() {
        expect:
        config.projectDir == null
        config.apiToken == null
        !config.createLocalRepo
        !config.cloneRemoteRepo
        !config.localOnly
        !config.createProject
        config.projectCreator == null
        !config.isCreateRemoteRepo()
        !config.isPublicProject()
        config.getRemoteServiceProvider().equals(GITHUB)

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
        config.cloneRemoteRepo(true).apiToken("token")

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
        config.cloneRemoteRepo(true).apiToken("token").projectName("dummy").remoteRepoFullName('davidsowerby/scratch')

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

    def "set and get"() {
        given:
        File f = new File('.')
        String projectName = 'wiggly'
        String repoFullName = 'ds/ds'
        String confirmDelete = 'ah, go on'

        when:
        config.projectDir(f)
        config.gitRemoteFactory(new DefaultGitRemoteFactory())
        config.projectName(projectName)
        config.localOnly(true)
        config.publicProject(true)
        config.remoteRepoFullName(repoFullName)
        config.confirmRemoteDelete(confirmDelete)
        config.remoteServiceProvider(GITHUB)

        then:
        config.getProjectDir().equals(f)
        config.getGitRemoteFactory() instanceof DefaultGitRemoteFactory
        config.getProjectName().equals(projectName)
        config.isLocalOnly()
        config.isPublicProject()
        config.getRemoteRepoFullName().equals(repoFullName)
        config.getConfirmRemoteDelete().equals(confirmDelete)
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
        config.getRemoteRepoUrl().equals("https://github.com/davidsowerby/scratch")
    }

    def "full repo url, no validation throws GitPlusConfigurationException"() {
        given:
        config.remoteRepoFullName("davidsowerby/scratch").projectName('scratch')

        when:
        config.getRemoteRepoUrl()

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
}
