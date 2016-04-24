package uk.q3c.build.gitplus.creator

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.util.testutil.FileTestUtil
import uk.q3c.util.testutil.TestResource

/**
 * Created by David Sowerby on 23 Apr 2016
 */
class GitIgnoreFileTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp
    GitIgnoreFile gitIgnoreFile

    def setup() {
        temp = temporaryFolder.root
        gitIgnoreFile = new GitIgnoreFile(temp)
    }

    def "full file"() {
        given:
        String buildFileName = '.gitignore'
        File expectedOutput = TestResource.resource(this, 'expected.gitignore')

        when:
        gitIgnoreFile
                .java()
                .idea()
                .eclipse()
                .entry('wiggly')
                .write()

        then:
        FileTestUtil.compare(new File(temp, buildFileName), expectedOutput)
    }
}