package uk.q3c.build.gitplus.local

import com.fasterxml.jackson.databind.ObjectMapper
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

    def "JSON export import"() {
        given: "change to a couple of non-default values"
        ObjectMapper objectMapper = new ObjectMapper()
        StringWriter sw = new StringWriter()
        configuration.projectName('wiggly')
        configuration.setVersion(23)

        when:
        objectMapper.writeValue(sw, configuration)
        DefaultGitLocalConfiguration configuration2 = objectMapper.readValue(sw.toString(), DefaultGitLocalConfiguration.class)

        then:
        configuration == configuration2
        configuration2.version == 23
    }

    def "copyFrom"() {
        given:
        DefaultGitLocalConfiguration configuration2 = new DefaultGitLocalConfiguration()

        when: "defaults compared"
        configuration2.copyFrom(configuration)

        then:
        configuration == configuration2

        when:
        configuration.projectName = "wiggly"

        then:
        configuration != configuration2

        when:
        configuration2.copyFrom(configuration)

        then:
        configuration == configuration2
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
