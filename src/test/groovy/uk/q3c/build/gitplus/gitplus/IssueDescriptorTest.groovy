package uk.q3c.build.gitplus.gitplus

import spock.lang.Specification

/**
 * Created by David Sowerby on 23 Sep 2017
 */
class IssueDescriptorTest extends Specification {

    def "github issue"() {
        given:
        RepoDescriptor repoDescriptor = new RepoDescriptor("https://github.com", "davidsowerby", "krail")
        IssueDescriptor issueDescriptor = new IssueDescriptor(repoDescriptor, 23)

        expect:
        repoDescriptor.toUrl() == "https://github.com/davidsowerby/krail"
        issueDescriptor.toUrl() == "https://github.com/davidsowerby/krail/issues/23"
    }

    def "bitbucket issue"() {
        RepoDescriptor repoDescriptor = new RepoDescriptor("https://bitbucket.org", "dxsowerby", "cpas")
        IssueDescriptor issueDescriptor = new IssueDescriptor(repoDescriptor, 9)

        expect:
        repoDescriptor.toUrl() == "https://bitbucket.org/dxsowerby/cpas"
        issueDescriptor.toUrl() == "https://bitbucket.org/dxsowerby/cpas/issues/9"
    }

}
