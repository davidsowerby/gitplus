package uk.q3c.build.gitplus

import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
/**
 * This test needs to delete the 'dummy' repo in cleanup.  This test is a bit weird because it has to use deleteRepo to clean up, but also tests deleteRepo
 *
 * Created by David Sowerby on 20 Mar 2016
 */
class GenerateOwnChangeLogTest extends Specification {

    def "generate own changelog"() {
        given:
        File userHome = new File(System.getProperty('user.home'))
        File projectDir = new File(userHome, 'git/gitplus')
        GitPlus gitPlus = new GitPlus();
        gitPlus.getConfiguration().remoteRepoFullName('davidsowerby/gitplus').projectDir(projectDir)

        expect:
        gitPlus.generateChangeLog()
    }




}