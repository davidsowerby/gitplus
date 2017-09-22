package uk.q3c.build.gitplus.gitplus

import spock.lang.Specification
import uk.q3c.build.gitplus.remote.ServiceProvider

/**
 * Created by David Sowerby on 21 Sep 2017
 */
class DefaultUrlParserTest extends Specification {

    UrlParser parser
    String url1 = "https://github.com/davidsowerby/kaytee-agent"
    String url2 = "https://example.com/davidsowerby/kaytee-agent"
    String url3 = "https://bitbucket.org/dxsowerby/cpas"
    String url4 = "https://github.com/davidsowerby"

    String urli1 = "https://github.com/davidsowerby/kaytee-agent/issues/14"
    String urli2 = "https://example.com/davidsowerby/kaytee-agent/issues/14"
    String urli3 = "https://bitbucket.org/dxsowerby/cpas/issues/9/find-a-sysadmin"
    String urli4 = "https://github.com/davidsowerby/krail/milestones/12"
    RepoDescriptor expectedGitHub
    RepoDescriptor expectedGitHubProvider
    RepoDescriptor expectedBitBucket

    def setup() {
        expectedGitHub = new RepoDescriptor("github.com", "davidsowerby", "kaytee-agent", ServiceProvider.GITHUB)
        expectedGitHubProvider = new RepoDescriptor("example.com", "davidsowerby", "kaytee-agent", ServiceProvider.GITHUB)
        expectedBitBucket = new RepoDescriptor("bitbucket.org", "dxsowerby", "cpas", ServiceProvider.BITBUCKET)
        parser = new DefaultUrlParser()
    }

    def "repo"() {


        when: "host is GitHub"
        RepoDescriptor actual = parser.repoDescriptor(url1)

        then: "then provider is GitHub"
        actual == expectedGitHub

        when: "host is unknown"
        actual = parser.repoDescriptor(url2)

        then: "then provider defaults to GitHub"
        actual == expectedGitHubProvider

        when: "host is BitBucket"
        actual = parser.repoDescriptor(url3)

        then: "then provider is BitBucket"
        actual == expectedBitBucket

        when: "url is not recognisable as a repo"
        actual = parser.repoDescriptor(url4)

        then: "then exception thrown"
        thrown RepoException

    }

    def "issue"() {
        given:

        IssueDescriptor expectedGitHubIssue = new IssueDescriptor(expectedGitHub, 14)
        IssueDescriptor expectedGitHubProviderIssue = new IssueDescriptor(expectedGitHubProvider, 14)
        IssueDescriptor expectedBitBucketIssue = new IssueDescriptor(expectedBitBucket, 9)

        when: "host is GitHub"
        IssueDescriptor actual = parser.issueDescriptor(urli1)

        then: "then provider is GitHub"
        actual == expectedGitHubIssue

        when: "host is unknown"
        actual = parser.issueDescriptor(urli2)

        then: "then provider defaults to GitHub"
        actual == expectedGitHubProviderIssue

        when: "host is BitBucket"
        actual = parser.issueDescriptor(urli3)

        then: "then provider is BitBucket"
        actual == expectedBitBucketIssue

        when: "url is not recognisable as an issue"
        actual = parser.issueDescriptor(urli4)

        then: "then exception thrown"
        thrown RepoIssueException

    }
}
