package uk.q3c.gitplus.remote

import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlusConfiguration

/**
 * Created by David Sowerby on 05 Apr 2016
 */
class DefaultRemoteRepoDeleteApproverTest extends Specification {

    GitPlusConfiguration configuration

    def setup() {
        configuration = new GitPlusConfiguration()
                .remoteRepoFullName('davidsowerby/dummy')
    }

    def "isApproved, rejected when no confirmation "() {

        expect:
        !configuration.deleteRepoApproved()
    }

    def "isApproved, rejected when confirmation incorrect"() {
        given:
        configuration.confirmRemoteDelete('something')

        expect:
        !configuration.deleteRepoApproved()
    }

    def "isApproved, approved when confirmation correct"() {
        given:
        configuration.confirmRemoteDelete('I really, really want to delete the davidsowerby/dummy repo from GitHub')

        expect:
        configuration.deleteRepoApproved()
    }


}
