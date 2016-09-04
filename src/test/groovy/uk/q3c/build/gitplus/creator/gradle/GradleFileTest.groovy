package uk.q3c.build.gitplus.creator.gradle

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.KotlinObjectFactory
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
        gradleFile = new DefaultGradleFile(temp)
        KotlinObjectFactory.fileBuffer().reset()
    }

    def "full file"() {
        given:
        String buildFileName = 'build.gradle'
        File expectedOutput = TestResource.resource(this, 'expected1.gradle')

        when:
        gradleFile.content
                .groovy('compile', '2.7')
                .junit('testCompile', '4.12')
                .spock('testCompile', 'groovy2.4')
        gradleFile.content.repositories('http://otherRepo').jcenter()
                .mavenCentral()
                .mavenLocal().end().dependencies().compile('org.slf4j:slf4j-api:1.7.5').end()
                .publishing(true).
                plugins('wiggly').idea().eclipse().end()
                .sourceCompatibility('1.8')
                .writeToFile(new File(temp, buildFileName))

        then:
        !FileTestUtil.compare(new File(temp, buildFileName), expectedOutput).isPresent()
    }

    def "default file name is build.gradle"() {
        expect:
        gradleFile.getFilename().equals('build.gradle')
    }

}
