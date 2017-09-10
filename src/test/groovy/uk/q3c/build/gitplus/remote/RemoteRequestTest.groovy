package uk.q3c.build.gitplus.remote

import com.jcabi.http.Request
import spock.lang.Specification
import uk.q3c.build.gitplus.remote.github.GitHubUrlMapper
import uk.q3c.build.gitplus.util.FilePropertiesLoader
import uk.q3c.build.gitplus.util.GitPlusProperty

/**
 * Created by David Sowerby on 26 Mar 2016
 */
class RemoteRequestTest extends Specification {

    def "request"() {
        given:
        String apiToken = new FilePropertiesLoader().getPropertyValue(GitPlusProperty.ISSUE_CREATE_TOKEN, ServiceProvider.GITHUB)

        expect:
        new DefaultRemoteRequest().request(Request.GET, GitHubUrlMapper.STATUS_API_URL, apiToken) != null
    }

}
