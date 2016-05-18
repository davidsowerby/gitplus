package uk.q3c.build.gitplus

import spock.lang.Specification
import uk.q3c.build.gitplus.creator.JavaSpockProjectCreator
import uk.q3c.build.gitplus.gitplus.GitPlus

/**
 * This test needs to delete the 'dummy' repo in cleanup.  This test is a bit weird because it has to use deleteRepo to clean up, but also tests deleteRepo
 *
 * Created by David Sowerby on 20 Mar 2016
 */
class CreateProjectTest extends Specification {

    def "create project"() {
        given:
        File home = new File(System.getProperty('user.home'))
        File projectDir = new File(home, 'git/simplycd')
        GitPlus gitPlus = new GitPlus();
        gitPlus.getConfiguration().remoteRepoFullName('davidsowerby/simplycd').publicProject(true).projectDir(projectDir)
                .createRemoteRepo(true).createLocalRepo(true).projectCreator(new JavaSpockProjectCreator(projectDir)).createProject(true)

        expect:
        gitPlus.createOrVerifyRepos()
    }

    def "creator"() {
        given:
        File home = new File(System.getProperty('user.home'))
        File projectDir = new File(home, 'git/simplycd')
        expect:
        def creator = new JavaSpockProjectCreator(projectDir)
        creator.prepare()
        creator.execute()
    }

}