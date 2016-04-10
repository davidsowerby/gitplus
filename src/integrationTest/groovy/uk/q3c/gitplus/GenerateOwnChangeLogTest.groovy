package uk.q3c.gitplus

import spock.lang.Specification
import uk.q3c.gitplus.changelog.ChangeLog
import uk.q3c.gitplus.changelog.ChangeLogConfiguration
import uk.q3c.gitplus.gitplus.GitPlus
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.local.GitLocalProvider

/**
 * This test needs to delete the 'dummy' repo in cleanup.  This test is a bit weird because it has to use deleteRepo to clean up, but also tests deleteRepo
 *
 * Created by David Sowerby on 20 Mar 2016
 */
class GenerateOwnChangeLogTest extends Specification {

    def "generate own changelog"() {
        given:
        File userHome = new File(System.getProperty('user.home'))
        File gitDir = new File(userHome, 'git')
        File projectDir = new File(gitDir, 'gitplus')
        GitPlusConfiguration gitPlusConfiguration = new GitPlusConfiguration()
                .remoteRepoFullName('davidsowerby/gitplus').projectDir(projectDir)
        GitPlus gitPlus = new GitPlus(gitPlusConfiguration, new GitLocalProvider())
        ChangeLogConfiguration changeLogConfiguration = new ChangeLogConfiguration()
        ChangeLog changeLog = new ChangeLog(gitPlus, changeLogConfiguration)

        expect:
        changeLog.createChangeLog()
    }


}