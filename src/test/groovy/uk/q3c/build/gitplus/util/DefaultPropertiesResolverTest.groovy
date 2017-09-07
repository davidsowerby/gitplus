package uk.q3c.build.gitplus.util

import com.google.common.collect.ImmutableList
import spock.lang.Specification
import uk.q3c.util.testutil.TestResource

import static uk.q3c.build.gitplus.remote.ServiceProvider.*

/**
 * Created by David Sowerby on 05 Sep 2017
 */
class DefaultPropertiesResolverTest extends Specification {

    PropertiesResolver resolver
    FilePropertiesLoader loader

    def setup() {
        File testFile = TestResource.resource(this, "gitplus.properties")
        loader = new FilePropertiesLoader().source(testFile)
        resolver = new DefaultPropertiesResolver()
    }

    def "properties are present"() {
        given:
        resolver.loaders = ImmutableList.of(loader)

        expect:
        resolver.taggerEmail() == "me@there"
        resolver.taggerName() == "me"
        resolver.apiTokenIssueCreate(GITHUB) == "issue create"
        resolver.apiTokenRepoDelete(GITHUB) == "repo delete"
        resolver.apiTokenRepoCreate(GITHUB) == "repo create"
    }

    def "properties missing"() {
        given:
        resolver.loaders = ImmutableList.of()

        when:
        resolver.apiTokenIssueCreate(GITHUB) == "issue create"

        then:
        thrown MissingPropertyException

    }
}
