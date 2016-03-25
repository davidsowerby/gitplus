package uk.q3c.gitplus

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.gitplus.changelog.ChangeLog
import uk.q3c.gitplus.changelog.ChangeLogConfiguration
import uk.q3c.gitplus.gitplus.GitPlus
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.local.GitLocal
import uk.q3c.gitplus.util.UserHomeBuildPropertiesLoader

/**
 * Created by David Sowerby on 07 Mar 2016
 */
class ChangeLogIntegrationTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    ChangeLog changeLog
    ChangeLogConfiguration changeLogConfiguration
    GitPlus gitPlus

    def setup() {
        temp = temporaryFolder.getRoot()
        File projectDir = new File("/home/david/git/q3c-testUtil")
        String apiToken = new UserHomeBuildPropertiesLoader().load().githubKeyRestricted()
        GitPlusConfiguration gitPlusConfiguration = new GitPlusConfiguration().projectDir(projectDir).apiToken(apiToken).remoteRepoFullName('davidsowerby/q3c-testUtil')
        gitPlus = new GitPlus(gitPlusConfiguration, new GitLocal(gitPlusConfiguration))
        changeLogConfiguration = new ChangeLogConfiguration()

    }

    def "run"() {
        given:
        File outputFile = new File(temp, 'changelog.md')
        changeLogConfiguration.outputFile(outputFile)
        changeLog = new ChangeLog(gitPlus, changeLogConfiguration)


        when:
        changeLog.createChangeLog()

        then:
        outputFile.exists()
    }

}