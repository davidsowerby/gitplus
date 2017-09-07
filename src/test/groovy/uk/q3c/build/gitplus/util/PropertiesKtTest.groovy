package uk.q3c.build.gitplus.util

import spock.lang.Specification
import uk.q3c.build.gitplus.remote.ServiceProvider
import uk.q3c.build.gitplus.remote.UnsupportedServiceProviderException

/**
 * Created by David Sowerby on 05 Sep 2017
 */
class PropertiesKtTest extends Specification {

    def "property lookup"() {
        expect:
        PropertiesKt.apiPropertyLookup(APIProperty.ISSUE_CREATE_TOKEN, ServiceProvider.GITHUB) == "GITHUB_ISSUE_CREATE_TOKEN"
//        PropertiesHandlerKt.apiPropertyLookup(APIProperty.REPO_CREATE_TOKEN,ServiceProvider.BITBUCKET)=="BITBUCKET_REPO_CREATE_TOKEN"
    }

    def "Unsupported provider"() {
        when:
        PropertiesKt.apiPropertyLookup(APIProperty.TAGGER_EMAIL, ServiceProvider.BITBUCKET) == "TAGGER_EMAIL"

        then:

        thrown UnsupportedServiceProviderException
    }
}
