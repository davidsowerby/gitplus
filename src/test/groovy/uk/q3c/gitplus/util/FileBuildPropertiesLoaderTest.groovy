package uk.q3c.gitplus.util

import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.gitplus.remote.UnsupportedServiceProviderException

import java.nio.file.Paths

import static uk.q3c.gitplus.remote.GitRemote.ServiceProvider.BITBUCKET
import static uk.q3c.gitplus.remote.GitRemote.ServiceProvider.GITHUB

/**
 * Created by David Sowerby on 22 Mar 2016
 */
class FileBuildPropertiesLoaderTest extends Specification {

    FileBuildPropertiesLoader loader

    def setup() {
        loader = new FileBuildPropertiesLoader();
    }

    def "get properties"() {
        given:
        loader.load()

        expect:
        loader.getProperties().size() >= 2
        loader.bintrayToken() != null
        loader.apiTokenRepoCreate(GITHUB) != null
        loader.apiTokenRestricted(GITHUB) != null
        loader.apiTokenRepoDelete(GITHUB) != null
    }

    def "Exception when called with unsupported provider"() {
        given:
        loader.load()

        when:
        loader.apiTokenRepoCreate(BITBUCKET)

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

    def "required property missing"() {
        given:
        loader.source(testResource('test1.properties'))

        when:
        loader.load()

        then:
        thrown GitPlusConfigurationException
    }

    def "Missing Property Exception if absent"() {
        given:
        loader.source(testResource('test3.properties')).load()


        when:
        loader.apiTokenRepoCreate(GITHUB)

        then:
        thrown(MissingPropertyException)
        true // bizarre way of making this test work - really should raise an issue on this
    }

    private File testResource(String fileName) {
        URL url = this.getClass()
                .getResource(fileName);
        return Paths.get(url.toURI())
                .toFile();
    }
}
