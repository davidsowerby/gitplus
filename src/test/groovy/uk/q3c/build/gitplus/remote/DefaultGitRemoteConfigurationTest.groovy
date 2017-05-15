package uk.q3c.build.gitplus.remote

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.ImmutableMap
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.local.GitLocal

import static uk.q3c.build.gitplus.ConstantsKt.notSpecified
/**
 * Created by David Sowerby on 31 Oct 2016
 */
class DefaultGitRemoteConfigurationTest extends Specification {

    RemoteRepoDeleteApprover approver = Mock(RemoteRepoDeleteApprover)
    GitRemoteConfiguration configuration

    void setup() {
        configuration = new DefaultGitRemoteConfiguration()
    }

    def "DeleteRepoApproved"() {
        given:
        approver.isApproved(configuration) >> approved

        when:
        configuration.repoDeleteApprover(approver)

        then:
        configuration.deleteRepoApproved() == result

        where:

        approved | result
        true     | true
        false    | false

    }

    def "JSON export import"() {
        given: "change to a couple of non-default values"
        ObjectMapper objectMapper = new ObjectMapper()
        StringWriter sw = new StringWriter()
        configuration.repoName('wiggly').projectHomePage('funky.pigeon')

        when:
        objectMapper.writeValue(sw, configuration)
        DefaultGitRemoteConfiguration configuration2 = objectMapper.readValue(sw.toString(), DefaultGitRemoteConfiguration.class)

        then:
        configuration.equals(configuration2)
    }

    def "IssueLabels"() {
        given:
        Map<String, String> map = ImmutableMap.of()

        when:
        configuration.issueLabels(map)

        then:
        configuration.issueLabels == map
    }

    def "MergeIssueLabels"() {

        when:
        configuration.mergeIssueLabels(true)

        then:
        configuration.mergeIssueLabels

        when:
        configuration.mergeIssueLabels(false)

        then:
        !configuration.mergeIssueLabels
    }

    def "Create"() {
        when:
        configuration.create(true)

        then:
        configuration.create

        when:
        configuration.create(false)

        then:
        !configuration.create
    }

    def "RepoName"() {
        when:
        configuration.repoName('a')

        then:
        configuration.repoName == 'a'
    }

    def "RepoUser"() {
        when:
        configuration.repoUser('a')

        then:
        configuration.repoUser == 'a'
    }

    def "ProjectDescription"() {
        when:
        configuration.projectDescription('a')

        then:
        configuration.projectDescription == 'a'
    }

    def "ProjectHomePage"() {
        when:
        configuration.projectHomePage('a')

        then:
        configuration.projectHomePage == 'a'
    }

    def "PublicProject"() {
        when:
        configuration.publicProject(true)

        then:
        configuration.publicProject

        when:
        configuration.publicProject(false)

        then:
        !configuration.publicProject
    }

    def "ConfirmDelete"() {

        when:
        configuration.confirmDelete('a')

        then:
        configuration.confirmDelete == 'a'
    }

    def "RemoteRepoFullName"() {
        when:
        configuration.repoUser('a')
        configuration.repoName('b')

        then:
        configuration.remoteRepoFullName() == 'a/b'
    }

    def "RepoDeleteApprover"() {
        given:
        RemoteRepoDeleteApprover mockApprover = Mock(RemoteRepoDeleteApprover)

        when:
        true

        then: "default"
        configuration.repoDeleteApprover instanceof DefaultRemoteRepoDeleteApprover

        when:
        configuration.repoDeleteApprover(mockApprover)

        then:

        configuration.repoDeleteApprover == mockApprover
    }

    def "SetupFromOrigin"() {

        when:
        configuration.setupFromOrigin('https://github.com/davidsowerby/krail.git')

        then:
        configuration.repoName == 'krail'
        configuration.repoUser == 'davidsowerby'
        configuration.providerBaseUrl == 'github.com'
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

    def "Copy"() {
        given:
        RemoteRepoDeleteApprover mockApprover = Mock(RemoteRepoDeleteApprover)
        GitRemoteConfiguration other = new DefaultGitRemoteConfiguration()
        other.repoUser = 'davidsowerby'
        other.repoName = 'krail'
        other.providerBaseUrl = 'other.github.com'
        other.projectDescription = 'projectDescription'
        other.projectHomePage = 'projectHomePage'
        other.publicProject = true
        other.issueLabels = ImmutableMap.of()
        other.mergeIssueLabels = true
        other.confirmDelete = 'a'
        other.create = true
        other.repoDeleteApprover = mockApprover
        other.active(false)

        when:
        configuration.copy(other)

        then:
        configuration.repoUser == 'davidsowerby'
        configuration.repoName == 'krail'
        configuration.providerBaseUrl == 'other.github.com'
        configuration.projectDescription == 'projectDescription'
        configuration.projectHomePage == 'projectHomePage'
        configuration.publicProject
        configuration.issueLabels == ImmutableMap.of()
        configuration.mergeIssueLabels
        configuration.confirmDelete == 'a'
        configuration.create
        configuration.repoDeleteApprover == mockApprover
        !configuration.active
    }

    def "repoName and repoUser must be specified, GitLocal.projectName not specified"() {
        given:
        GitLocal local = Mock(GitLocal)
        local.projectName >> notSpecified

        configuration.repoName("any")
        configuration.repoUser("someone")

        when:
        configuration.validate(local)

        then:
        noExceptionThrown()

        when:
        configuration.repoName(notSpecified)
        configuration.validate(local)

        then:
        thrown GitPlusConfigurationException

        when:
        configuration.repoName('any')
        configuration.repoUser(notSpecified)
        configuration.validate(local)

        then:
        thrown GitPlusConfigurationException
    }

    def "local.projectName used if repoName not specified"() {
        given:
        GitLocal local = Mock(GitLocal)
        local.projectName >> 'wiggly'
        configuration.repoUser("someone")

        when:
        configuration.validate(local)

        then:
        configuration.repoName == 'wiggly'
    }
}
