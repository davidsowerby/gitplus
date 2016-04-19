package uk.q3c.build.gitplus.remote

import com.google.common.collect.ImmutableSet
import spock.lang.Specification

/**
 * Created by David Sowerby on 25 Mar 2016
 */
class GPIssueTest extends Specification {

    def "compareTo same"() {
        given:
        GPIssue issue1 = new GPIssue(1).htmlUrl('a')
        GPIssue issue2 = new GPIssue(1).htmlUrl('a')

        expect:
        issue1.compareTo(issue2) == 0
    }

    def "compareTo different number only"() {
        given:
        GPIssue issue1 = new GPIssue(1).htmlUrl('a')
        GPIssue issue2 = new GPIssue(2).htmlUrl('a')

        expect:
        issue1.compareTo(issue2) == -1
    }

    def "compareTo different url only"() {
        given:
        GPIssue issue1 = new GPIssue(1).htmlUrl('a')
        GPIssue issue2 = new GPIssue(1).htmlUrl('b')

        expect:
        issue1.compareTo(issue2) == -1
        issue1.compareTo(null) == -1
    }

    def "equals and hashcode, url and number the same"() {
        given:
        GPIssue issue1 = new GPIssue(1).htmlUrl('a')
        GPIssue issue2 = new GPIssue(1).htmlUrl('a')
        GPIssue issue3 = new GPIssue(2).htmlUrl('a')
        GPIssue issue4 = new GPIssue(1).htmlUrl('b')

        expect:
        issue1.equals(issue2)
        issue1.hashCode() == issue2.hashCode()
        !issue1.equals(issue3)
        issue1.hashCode() != issue3.hashCode()
        !issue1.equals(issue4)
        issue1.hashCode() != issue4.hashCode()
        !issue1.equals(null)
    }

    def "set and get"() {
        given:
        GPIssue issue1 = new GPIssue(1).htmlUrl('a').body('b').labels(ImmutableSet.of('a', 'b')).pullRequest(true)


        expect:
        issue1.number == 1
        issue1.getHtmlUrl().equals('a')
        issue1.getBody().equals('b')
        issue1.hasLabel('a')
        !issue1.hasLabel('c')
        issue1.isPullRequest()

    }
}
