package uk.q3c.build.gitplus.creator

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

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

    def "create"() {
        given:
        creator.prepare()

        when:
        creator.execute()

        then:
        new File(temp, 'src/main/java').exists()
        new File(temp, 'src/main/resources').exists()
        new File(temp, 'src/test/groovy').exists()
        new File(temp, 'src/test/resources').exists()
        new File(temp, '.gitignore').exists()
        new File(temp, 'build.gradle').exists()
        creator.getGradleFile().getFilename().equals('build.gradle')
        creator.getGitIgnoreFle().getFilename().equals('.gitignore')
    }
}
