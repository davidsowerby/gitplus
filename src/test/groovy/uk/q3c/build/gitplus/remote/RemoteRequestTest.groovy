package uk.q3c.build.gitplus.remote

import com.jcabi.http.Request
import spock.lang.Specification
import uk.q3c.build.gitplus.util.FileBuildPropertiesLoader

import static uk.q3c.build.gitplus.remote.GitRemote.ServiceProvider.GITHUB

/**
 * Created by David Sowerby on 26 Mar 2016
 */
class RemoteRequestTest extends Specification {

    def "request"() {
        given:
        String apiToken = new FileBuildPropertiesLoader().load().apiTokenRestricted(GITHUB)

        expect:
        new RemoteRequest().request(Request.GET, GitHubRemote.STATUS_API_URL, apiToken) != null
    }

}
