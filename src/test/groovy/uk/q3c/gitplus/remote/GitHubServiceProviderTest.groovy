package uk.q3c.gitplus.remote

import org.kohsuke.github.GitHub
import spock.lang.Specification
import uk.q3c.gitplus.util.UserHomeBuildPropertiesLoader

/**
 * Created by David Sowerby on 25 Mar 2016
 */
class GitHubServiceProviderTest extends Specification {

    def "get"() {
        given:
        String apiToken = new UserHomeBuildPropertiesLoader().load().githubKeyRestricted()
        when:
        GitHub gitHub = new GitHubProvider().get(apiToken)

        then:
        gitHub != null
    }
}
