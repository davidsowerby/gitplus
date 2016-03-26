package uk.q3c.gitplus.remote

import com.jcabi.http.Request
import spock.lang.Specification
import uk.q3c.gitplus.util.UserHomeBuildPropertiesLoader

/**
 * Created by David Sowerby on 26 Mar 2016
 */
class RemoteRequestTest extends Specification {

    def "request"() {
        given:
        String apiToken = new UserHomeBuildPropertiesLoader().load().githubKeyRestricted()

        expect:
        new RemoteRequest().request(Request.GET, GitHubRemote.STATUS_API_URL, apiToken) != null
    }
}
