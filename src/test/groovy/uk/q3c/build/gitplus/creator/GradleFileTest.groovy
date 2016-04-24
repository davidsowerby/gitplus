package uk.q3c.build.gitplus.creator

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.util.testutil.FileTestUtil
import uk.q3c.util.testutil.TestResource

/**
 * Created by David Sowerby on 23 Apr 2016
 */
class GradleFileTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp
    GradleFile gradleFile

    def setup() {
        temp = temporaryFolder.root
        gradleFile = new GradleFile(temp)
    }

    def "full file"() {
        given:
        String buildFileName = 'other.gradle'
        File expectedOutput = TestResource.resource(this, 'expected.gradle')

        when:
        gradleFile
                .java()
                .groovy()
                .junit()
                .spock()
                .jcenter()
                .mavenCentral()
                .mavenLocal()
                .compileDependency('org.slf4j:slf4j-api:1.7.5')
                .publishing(true)
                .idea()
                .eclipse()
                .plugin('wiggly')
                .filename(buildFileName)
                .repository('http://otherRepo')
                .sourceCompatibility('1.8')
                .write()

        then:
        FileTestUtil.compare(new File(temp, buildFileName), expectedOutput)
    }

    def "default file name is build.gradle"() {
        expect:
        gradleFile.getFilename().equals('build.gradle')
    }

}
