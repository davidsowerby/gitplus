package uk.q3c.build.gitplus.test

import spock.lang.Specification
import uk.q3c.build.gitplus.local.GitLocalConfiguration
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration

import static org.mockito.Mockito.*

/**
 * Created by David Sowerby on 04 Sep 2017
 */
class MocksKtTest extends Specification {

    def "mock gitplus with data configuration"() {
        given:
        GitLocalConfiguration localConfig = MocksKt.mockGitPlusWithDataConfig().local.configuration
        GitRemoteConfiguration remoteConfig = MocksKt.mockGitPlusWithDataConfig().remote.configuration

        when: "attempt to mock a real config"
        localConfig.active(false)
        remoteConfig.active(false)


        then: "Would not hold value if a mock"
        !localConfig.active
        !remoteConfig.active
    }


    def "mock GitPlus with mock configuration"() {
        given:
        GitLocalConfiguration localConfig = MocksKt.mockGitPlusWithMockConfig().local.configuration
        GitRemoteConfiguration remoteConfig = MocksKt.mockGitPlusWithMockConfig().remote.configuration
        when(localConfig.active(false)).thenThrow(new RuntimeException("fake"))
        when(remoteConfig.active(false)).thenThrow(new RuntimeException("fake"))

        when:
        localConfig.active(false)

        then:
        thrown RuntimeException

        when:
        remoteConfig.active(false)

        then:
        thrown RuntimeException
    }

    def "mock gitLocal and remote with data configuration"() {
        given:
        GitLocalConfiguration localConfig = MocksKt.mockGitLocalWithDataConfig()
        GitRemoteConfiguration remoteConfig = MocksKt.mockGitRemoteWithDataConfig()

        when: "attempt to mock a real config"
        localConfig.active(false)
        remoteConfig.active(false)


        then: "Would not hold value if a mock"
        !localConfig.active
        !remoteConfig.active
    }

    def "mock gitLocal and remote with mock configuration"() {
        given:
        GitLocalConfiguration localConfig = MocksKt.mockGitLocalWithMockConfig()
        GitRemoteConfiguration remoteConfig = MocksKt.mockGitRemoteWithMockConfig()
        when(localConfig.active(false)).thenThrow(new RuntimeException("fake"))
        when(remoteConfig.active(false)).thenThrow(new RuntimeException("fake"))

        when:
        localConfig.active(false)

        then:
        thrown RuntimeException

        when:
        remoteConfig.active(false)

        then:
        thrown RuntimeException
    }
}
