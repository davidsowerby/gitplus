package uk.q3c.gitplus.origin

import spock.lang.Specification
import uk.q3c.gitplus.util.PropertyException

/**
 * Created by David Sowerby on 11 Mar 2016
 */
class GitHubServiceApiTest extends Specification {

    OriginServiceApi api

    def setup() {
        api = new GitHubServiceApi()
    }

    def "getIssue with repoName not set throws PropertyException"() {
        when:
        api.getIssue(2)

        then:
        thrown(PropertyException)

    }
}
