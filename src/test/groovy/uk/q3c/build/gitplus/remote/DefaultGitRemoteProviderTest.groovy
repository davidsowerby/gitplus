package uk.q3c.build.gitplus.remote

import com.google.inject.Provider
import spock.lang.Specification
import uk.q3c.build.gitplus.remote.bitbucket.BitBucketRemote
import uk.q3c.build.gitplus.remote.bitbucket.BitBucketUrlMapper
import uk.q3c.build.gitplus.remote.bitbucket.DefaultBitBucketProvider
import uk.q3c.build.gitplus.remote.bitbucket.DefaultBitBucketRemote
import uk.q3c.build.gitplus.remote.github.DefaultGitHubProvider
import uk.q3c.build.gitplus.remote.github.DefaultGitHubRemote
import uk.q3c.build.gitplus.remote.github.GitHubRemote
import uk.q3c.build.gitplus.remote.github.GitHubUrlMapper
import uk.q3c.build.gitplus.util.FileBuildPropertiesLoader

import static uk.q3c.build.gitplus.remote.ServiceProvider.BITBUCKET
import static uk.q3c.build.gitplus.remote.ServiceProvider.GITHUB

/**
 * Created by David Sowerby on 26 Oct 2016
 */
class DefaultGitRemoteProviderTest extends Specification {


    static GitRemoteConfiguration gitRemoteConfiguration1 = new DefaultGitRemoteConfiguration()
    GitRemoteConfiguration gitRemoteConfiguration2 = new DefaultGitRemoteConfiguration()


    Map<ServiceProvider, Provider<GitRemote>> serviceProviders

    GitHubRemote gitHubRemote = new DefaultGitHubRemote(gitRemoteConfiguration2, new DefaultGitHubProvider(new FileBuildPropertiesLoader()), new DefaultRemoteRequest(), new GitHubUrlMapper())
    Provider<GitHubRemote> gitHubProvider = Mock(Provider)

    BitBucketRemote bitBucketRemote = new DefaultBitBucketRemote(gitRemoteConfiguration2, new DefaultBitBucketProvider(), new DefaultRemoteRequest(), new BitBucketUrlMapper())
    Provider<BitBucketRemote> bitBucketProvider = Mock(Provider)

    def setup() {
        gitHubProvider.get() >> gitHubRemote
        bitBucketProvider.get() >> bitBucketRemote
        serviceProviders = new HashMap<>()
        serviceProviders.put(GITHUB, gitHubProvider)
        serviceProviders.put(BITBUCKET, bitBucketProvider)

    }

    def "change remote service provider"() {

        given:
        gitRemoteConfiguration1.repoUser('d').repoName('x')

        when: "default"
        GitRemoteProvider provider = new DefaultGitRemoteProvider(serviceProviders)


        then:
        GitRemote remote = provider.get(providerId, configParam)
        instanceType.isAssignableFrom(remote.class)
        remote.configuration.repoUser == configParam.repoUser
        remote.configuration.repoName == configParam.repoName


        where:

        providerId | configParam             | instanceType
        GITHUB     | gitRemoteConfiguration1 | GitHubRemote.class
        BITBUCKET  | gitRemoteConfiguration1 | BitBucketRemote.class

    }
}
