package uk.q3c.gitplus.origin

import org.kohsuke.github.GHIssue
import spock.lang.Specification
import uk.q3c.gitplus.util.DefaultBuildPropertiesLoader

/**
 * Created by David Sowerby on 12 Feb 2016
 */
class GitHubHandlerTest extends Specification {

    GitHubServiceApi api

    def setup() {
        api = new GitHubServiceApi()
        DefaultBuildPropertiesLoader loader = new DefaultBuildPropertiesLoader().load();
        api.setApiToken(loader.githubKey())
    }

    def "rate limit remaining"() {
        when:
        int remaining = api.remainingCalls()
        int remaining1 = api.remainingCalls()

        then:
        remaining > 0
        remaining <= 5000
        remaining == remaining1
    }

    def "rate limit"() {}

    def "rate limit reset"() {
        when:

        int tol = api.timeOfRateLimitReset()

        then:
        tol > 0
    }


    def "api status"() {
        expect:
        api.apiStatus() == GitHubServiceApi.Status.GREEN
    }


    def "create issue"() {
        given:
        api.setRepoName("davidsowerby/scratch")
        String title = "test issue"
        String body = "body"
        String label = "buglet"

        when:
        GHIssue result = api.createIssue(title, body, label)

        then:
        result.getNumber() > 0
        result.getTitle().equals(title)
        result.getLabels().get(0).getName().equals(label)
        result.getBody().equals(body)
        result.getLabels().size() == 1
    }


}
