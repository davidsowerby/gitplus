package uk.q3c.build.gitplus

import spock.lang.Specification

/**
 * Created by David Sowerby on 03 Nov 2016
 */
class GitSHATest extends Specification {


    def "too long"() {
        given:
        String block = '0123456789'
        String sha = block + block + block + block + '0'

        when:
        new GitSHA(sha)

        then:
        thrown GitSHAException
    }

    def "too short"() {
        given:
        String block = '0123456789'
        String sha = block + block + block + '012345678'

        when:
        new GitSHA(sha)

        then:
        thrown GitSHAException
    }

    def "correct length and valid hex"() {
        given:
        String block = '01234567aB'
        String sha = block + block + block + block

        when:
        GitSHA result = new GitSHA(sha)

        then:
        noExceptionThrown()
        result.sha == result.toString()

    }

    def "short"() {
        given:
        String block = '01234567aB'
        String sha = block + block + block + block

        when:
        GitSHA result = new GitSHA(sha)

        then:
        result.short().length() == 7
        result.short() == '0123456'
    }

    def "invalid hex"() {
        given:

        String block = '0123456789'
        String sha = block + block + block + '012345678k'

        when:
        new GitSHA(sha)

        then:
        thrown GitSHAException
    }


}
