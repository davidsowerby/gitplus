package uk.q3c.build.gitplus.creator

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.util.testutil.FileTestUtil
import uk.q3c.util.testutil.TestResource
/**
 * Created by David Sowerby on 24 Apr 2016
 */
class JavaSpockProjectCreatorTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    JavaSpockProjectCreator creator

    def setup() {
        temp = temporaryFolder.root
        creator = new JavaSpockProjectCreator(temp)
    }

    def "create with defaults"() {
        given:
        creator.prepare()

        when:
        creator.execute()

        then:
        new File(temp, 'src/main/java/DummyJava.java').exists()
        new File(temp, 'src/main/resources/DummyResource.txt').exists()
        new File(temp, 'src/test/groovy/DummyTestGroovy.groovy').exists()
        new File(temp, 'src/test/resources/DummyTestResource.txt').exists()
        new File(temp, '.gitignore').exists()
        new File(temp, 'build.gradle').exists()
        creator.getGradleFile().getFilename().equals('build.gradle')
        creator.getGitIgnoreFile().getFilename().equals('.gitignore')


        File expected = TestResource.resource(this, 'expected.gradle')
        File actual = new File(temp, 'build.gradle')
        !FileTestUtil.compare(actual, expected).isPresent()
    }
}
