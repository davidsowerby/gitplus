package uk.q3c.build.gitplus.local

import spock.lang.Specification

/**
 * Created by David Sowerby on 17 May 2017
 */
class DefaultGitInitCheckerTest extends Specification {

    DefaultGitInitChecker initChecker

    def setup() {
        initChecker = new DefaultGitInitChecker()
    }

    def "reset"() {
        given:
        initChecker.reset()

        expect:
        !initChecker.initDone
    }
}
