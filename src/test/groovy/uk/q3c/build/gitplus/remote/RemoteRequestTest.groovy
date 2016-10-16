package uk.q3c.build.gitplus.remote

import com.jcabi.http.Request
import spock.lang.Specification
import uk.q3c.build.gitplus.remote.github.GitHubUrlMapper
import uk.q3c.build.gitplus.util.FileBuildPropertiesLoader


/**
 * Created by David Sowerby on 26 Mar 2016
 */
class RemoteRequestTest extends Specification {

    def "request"() {
        given:
        String apiToken = new FileBuildPropertiesLoader().load().apiTokenRestricted(ServiceProvider.GITHUB)

        expect:
        new DefaultRemoteRequest().request(Request.GET, GitHubUrlMapper.STATUS_API_URL, apiToken) != null
    }

}
