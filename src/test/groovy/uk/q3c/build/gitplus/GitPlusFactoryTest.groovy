package uk.q3c.build.gitplus

import org.apache.commons.io.FileUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitLocal
/**
 * Created by David Sowerby on 21 Dec 2016
 */
class GitPlusFactoryTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    def setup() {
        temp = temporaryFolder.getRoot()
    }

    def "getInstance"() {

        expect:
        GitPlusFactory.instance != null
    }

    def "getInstance and use GitLocal"() {
        given:
        GitPlus gitPlus = GitPlusFactory.instance
        FileUtils.forceMkdir(new File(temp, 'wiggly'))

        when:
        GitLocal local = gitPlus.local
        local.configuration.projectDirParent = temp
        local.configuration.projectName = 'wiggly'
        local.prepare(gitPlus)

        then:
        local.status().changed.isEmpty()

    }

    def "prepare() has invalid config"() {
        given:
        GitPlus gitPlus = GitPlusFactory.instance

        when:
        GitLocal local = gitPlus.local
        local.configuration.projectDirParent = temp
        local.configuration.projectName = 'wiggly'
        local.prepare(gitPlus)

        then:
        local.status().changed.isEmpty()
    }

    def "execute does not look for API tokens"() {
        given:
        GitPlus gitPlus = GitPlusFactory.instance
        GitLocal local = gitPlus.local
        local.configuration.projectDirParent = temp
        local.configuration.projectName = 'wiggly'
        gitPlus.remote.repoUser = "davidsowerby"
        gitPlus.remote.repoName = "scratch"

        when:
        gitPlus.execute()

        then:
        noExceptionThrown()
    }
}
