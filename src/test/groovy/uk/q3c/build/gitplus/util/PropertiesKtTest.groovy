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
        PropertiesKt.propertyLookup(GitPlusProperty.ISSUE_CREATE_TOKEN, ServiceProvider.GITHUB) == "github-issue-create-token"
//        PropertiesHandlerKt.apiPropertyLookup(GitPlusProperty.REPO_CREATE_TOKEN,ServiceProvider.BITBUCKET)=="BITBUCKET_REPO_CREATE_TOKEN"
    }

    def "Unsupported provider"() {
        when:
        PropertiesKt.propertyLookup(GitPlusProperty.TAGGER_EMAIL, ServiceProvider.BITBUCKET) == "tagger-email"

        then:

        thrown UnsupportedServiceProviderException
    }
}
