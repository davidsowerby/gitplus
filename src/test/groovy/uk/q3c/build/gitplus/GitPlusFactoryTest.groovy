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
        local.localConfiguration.projectDirParent = temp
        local.localConfiguration.projectName = 'wiggly'
        local.prepare(gitPlus.remote)

        then:
        local.status().changed.isEmpty()

    }

    def "prepare() has invalid config"() {
        given:
        GitPlus gitPlus = GitPlusFactory.instance

        when:
        GitLocal local = gitPlus.local
        local.localConfiguration.projectDirParent = temp
        local.localConfiguration.projectName = 'wiggly'
        local.prepare(gitPlus.remote)

        then:
        local.status().changed.isEmpty()
    }

}
