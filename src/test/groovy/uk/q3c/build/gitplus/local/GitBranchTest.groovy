package uk.q3c.build.gitplus.local

import spock.lang.Specification

/**
 * Created by David Sowerby on 31 Oct 2016
 */
class GitBranchTest extends Specification {

    def "ref"() {
        given:
        GitBranch branch = new GitBranch('develop')

        expect:
        branch.ref() == 'refs/heads/develop'
    }
}
