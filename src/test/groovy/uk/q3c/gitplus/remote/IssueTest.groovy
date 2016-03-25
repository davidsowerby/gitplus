package uk.q3c.gitplus.remote

import spock.lang.Specification

/**
 * Created by David Sowerby on 25 Mar 2016
 */
class IssueTest extends Specification {

    def "compareTo same"() {
        given:
        Issue issue1 = new Issue(1).htmlUrl("a")
        Issue issue2 = new Issue(1).htmlUrl("a")

        expect:
        issue1.compareTo(issue2) == 0
    }

    def "compareTo different number only"() {
        given:
        Issue issue1 = new Issue(1).htmlUrl("a")
        Issue issue2 = new Issue(2).htmlUrl("a")

        expect:
        issue1.compareTo(issue2) == -1
    }

    def "compareTo different url only"() {
        given:
        Issue issue1 = new Issue(1).htmlUrl("a")
        Issue issue2 = new Issue(1).htmlUrl("b")

        expect:
        issue1.compareTo(issue2) == -1
    }

    def "equals and hashcode, url and number the same"() {
        given:
        Issue issue1 = new Issue(1).htmlUrl("a")
        Issue issue2 = new Issue(1).htmlUrl("a")
        Issue issue3 = new Issue(2).htmlUrl("a")
        Issue issue4 = new Issue(1).htmlUrl("b")

        expect:
        issue1.equals(issue2)
        issue1.hashCode() == issue2.hashCode()
        !issue1.equals(issue3)
        issue1.hashCode() != issue3.hashCode()
        !issue1.equals(issue4)
        issue1.hashCode() != issue4.hashCode()
    }
}
