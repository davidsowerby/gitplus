package uk.q3c.build.gitplus.util

import org.apache.commons.io.FileUtils
import spock.lang.Specification
import uk.q3c.build.gitplus.ConstantsKt
import uk.q3c.build.gitplus.remote.UnsupportedServiceProviderException
import uk.q3c.util.testutil.TestResource

import static uk.q3c.build.gitplus.remote.ServiceProvider.*
import static uk.q3c.build.gitplus.util.APIProperty.*

/**
 * Created by David Sowerby on 22 Mar 2016
 */
class FilePropertiesLoaderTest extends Specification {

    FilePropertiesLoader loader

    def setup() {
        loader = new FilePropertiesLoader()
        loader.source = TestResource.resource(this, "gitplus.properties")
    }

    def "get properties"() {
        given:
        loader.load()

        expect:
        loader.getProperties().size() >= 2
        loader.bintrayToken() != null
        loader.getPropertyValue(APIProperty.REPO_CREATE_TOKEN, GITHUB) != null
        loader.getPropertyValue(APIProperty.ISSUE_CREATE_TOKEN, GITHUB) != null
        loader.getPropertyValue(APIProperty.REPO_DELETE_TOKEN, GITHUB) != null
    }

    def "Exception when called with unsupported provider"() {
        given:
        loader.load()

        when:
        loader.getPropertyValue(APIProperty.REPO_CREATE_TOKEN, BITBUCKET)

        then:
        thrown UnsupportedServiceProviderException
    }

    def "set and get source"() {
        given:
        final String filename = 'test1.properties'
        final File source = testResource(filename)
        loader.source(source)

        expect:
        loader.getSource().equals(source)

    }


    def "if token absent return 'notSpecified'"() {
        given:
        loader.source(testResource('test3.properties'))


        expect:
        loader.getPropertyValue(APIProperty.REPO_CREATE_TOKEN, GITHUB) == ConstantsKt.notSpecified
        loader.getPropertyValue(APIProperty.ISSUE_CREATE_TOKEN, GITHUB) == ConstantsKt.notSpecified
        loader.getPropertyValue(APIProperty.REPO_DELETE_TOKEN, GITHUB) == ConstantsKt.notSpecified

    }


    def "tagger name and email"() {
        given:
        loader.source(testResource('test1.properties'))

        expect:
        loader.getPropertyValue(TAGGER_NAME, GITHUB) == ('Ronnie Corbett')
        loader.getPropertyValue(TAGGER_EMAIL, GITHUB) == ('forkhandles@there.com')
    }

    def "tagger name or email missing returns notSpecified"() {
        given:
        loader.source(testResource('test3.properties'))


        expect:
        loader.getPropertyValue(TAGGER_NAME, GITHUB) == ConstantsKt.notSpecified
        loader.getPropertyValue(TAGGER_EMAIL, GITHUB) == ConstantsKt.notSpecified

    }


    def "invalid file, load returns false and property 'unspecified"() {
        given:
        loader.source(new File('test4.properties'))

        expect:
        loader.getPropertyValue(TAGGER_NAME, GITHUB) == ConstantsKt.notSpecified
    }

    def "change source"() {
        given:
        File someFile = new File(FileUtils.userDirectory, "some.properties")

        when:
        loader.source(someFile)

        then:
        loader.source == someFile

        when:
        loader.source(someFile.absolutePath)

        then:
        loader.source == someFile

        when:
        loader.sourceFromGradle()

        then:
        loader.source == new File(FileUtils.userDirectory, "gradle/gradle.properties")

        when:
        loader.sourceFromGitPlus()

        then:
        loader.source == new File(FileUtils.userDirectory, "gitplus/gitplus.properties")
    }

    private File testResource(String fileName) {
        return TestResource.resource(this, fileName)
    }
}
