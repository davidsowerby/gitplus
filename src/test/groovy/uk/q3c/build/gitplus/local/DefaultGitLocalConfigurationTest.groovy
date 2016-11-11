package uk.q3c.build.gitplus.local

import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.remote.GitRemote

/**
 * Created by David Sowerby on 24 Oct 2016
 */
class DefaultGitLocalConfigurationTest extends Specification {

    DefaultGitLocalConfiguration configuration

    ProjectCreator mockProjectCreator = Mock(ProjectCreator)

    void setup() {
        configuration = new DefaultGitLocalConfiguration()
    }

    def "create and cloneFromRemote cannot both be true"() {

        given:
        GitRemote remote = Mock(GitRemote)

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
        configuration.validate(remote)

        then: "exception because both are true"
        thrown GitPlusConfigurationException

    }

    def "setters"() {
        when: // default
        true

        then:
        configuration.active
        configuration.projectCreator instanceof DefaultProjectCreator

        when:
        configuration.active(false)
        configuration.projectCreator(mockProjectCreator)

        then:
        !configuration.active
        configuration.projectCreator == mockProjectCreator
    }

}
