package uk.q3c.build.gitplus.remote

import spock.lang.Specification
/**
 * Created by David Sowerby on 05 Apr 2016
 */
class DefaultRemoteRepoDeleteApproverTest extends Specification {

    DefaultGitRemoteConfiguration configuration

    def setup() {
        configuration = new DefaultGitRemoteConfiguration()
        configuration.repoUser('davidsowerby').repoName('dummy')
    }

    def "isApproved, rejected when no confirmation "() {

        expect:
        !configuration.deleteRepoApproved()
    }

    def "isApproved, rejected when confirmation incorrect"() {
        given:
        configuration.confirmDelete('something')

        expect:
        !configuration.deleteRepoApproved()
    }

    def "isApproved, approved when confirmation correct"() {
        given:
        configuration.confirmDelete('I really, really want to delete the davidsowerby/dummy repo from GitHub')

        expect:
        configuration.deleteRepoApproved()
    }


}
