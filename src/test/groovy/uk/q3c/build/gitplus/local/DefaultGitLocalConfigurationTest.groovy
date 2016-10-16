package uk.q3c.build.gitplus.local

import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException

/**
 * Created by David Sowerby on 24 Oct 2016
 */
class DefaultGitLocalConfigurationTest extends Specification {

    DefaultGitLocalConfiguration configuration

    void setup() {
        configuration = new DefaultGitLocalConfiguration()
    }

    def "create and cloneFromRemote cannot both be true"() {

        when:
        true

        then: "defaults"
        !configuration.create
        !configuration.cloneFromRemote

        when:
        configuration.create(true)

        then:
        configuration.create
        !configuration.cloneFromRemote

        when:
        configuration.cloneFromRemote = true

        then: "both true"
        configuration.create
        configuration.cloneFromRemote

        when:
        configuration.validate()

        then: "exception because both are true"
        thrown GitPlusConfigurationException

    }

    def "setters"() {
        when: // default
        true

        then:
        configuration.active

        when:
        configuration.active(false)

        then:
        !configuration.active
    }
}
