package uk.q3c.build.gitplus.remote.github

import com.jcabi.github.RtGithub
import spock.lang.IgnoreIf
import spock.lang.Specification
import uk.q3c.build.gitplus.GitPlusFactory
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.remote.DefaultGitRemoteConfiguration
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration
import uk.q3c.build.gitplus.test.TestEnvironmentCheck
import uk.q3c.build.gitplus.util.FilePropertiesLoader

/**
 * This test accesses GitHub and would fail if there is no properties file with valid API tokens.  For collaborators,
 * that may well be desirable, so this test therefore checks for the existence of tokens in a file in the default location,
 * and in <user.home>/gradle/gradle.properties
 *
 * If not there, the test is passed and a warning logged
 *
 *
 * Created by David Sowerby on 25 Mar 2016
 */
class GitHubServiceProviderTest extends Specification {

    File gradlePropertiesFile
    GitPlus gitPlus

    def setup() {
        File userHome = new File(System.getProperty("user.home"))
        gradlePropertiesFile = new File(userHome, "gradle/gradle.properties")
        gitPlus = GitPlusFactory.instance
        gitPlus.propertiesFromGradle().remote.configuration.repoName("q3c-testutils").repoUser("davidsowerby")
        gitPlus.execute()

    }

    @IgnoreIf({ TestEnvironmentCheck.tokensNotAvailable })
    def "get"() {
        given:
        GitHubProvider provider = new DefaultGitHubProvider()

        when:
        true
        //do nothing

        then:

        provider.get(gitPlus, GitRemote.TokenScope.CREATE_ISSUE) instanceof RtGithub
        provider.get(gitPlus, GitRemote.TokenScope.CREATE_REPO) instanceof RtGithub
        provider.get(gitPlus, GitRemote.TokenScope.DELETE_REPO) instanceof RtGithub

    }


    private GitRemoteConfiguration dummyConfiguration() {
        DefaultGitRemoteConfiguration dummyConfiguration = new DefaultGitRemoteConfiguration()
        dummyConfiguration.apiPropertiesLoaders.add(new FilePropertiesLoader().source(gradlePropertiesFile))
        return dummyConfiguration
    }
}
