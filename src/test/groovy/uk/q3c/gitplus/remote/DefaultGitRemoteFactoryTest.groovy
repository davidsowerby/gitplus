package uk.q3c.gitplus.remote

import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.gitplus.GitPlusConfigurationException

/**
 * Created by David Sowerby on 18 Mar 2016
 */
class DefaultGitRemoteFactoryTest extends Specification {

    GitPlusConfiguration gitPlusConfiguration
    GitRemote.Provider provider = GitRemote.Provider.GITHUB

    def setup() {
        gitPlusConfiguration = new GitPlusConfiguration().apiToken('xxxxxxxxx')
    }

    def "create"() {
        expect:
        new DefaultGitRemoteFactory().create(gitPlusConfiguration) instanceof GitHubRemote
    }

    def "Urls"() {
        given:
        GitRemoteFactory factory = new DefaultGitRemoteFactory()
        String fullRepoName = 'davidsowerby/krail'

        expect:
        factory.htmlUrlStem(provider).equals("https://github.com")
        factory.apiUrlStem(provider).equals("https://api.github.com")
        factory.cloneUrl(provider, fullRepoName).equals("https://github.com/davidsowerby/krail.git")
        factory.htmlUrlFromFullRepoName(provider, fullRepoName).equals("https://github.com/davidsowerby/krail")
        factory.htmlTagUrl(provider, fullRepoName).equals("https://github.com/davidsowerby/krail/tree")
        factory.repoFullNameFromHtmlUrl(provider, fullRepoName).equals("davidsowerby/krail")
        factory.projectNameFromRemoteRepFullName(provider, fullRepoName).equals('krail')
    }

    def "malformed full repo name throws NPE"() {
        given:
        GitRemoteFactory factory = new DefaultGitRemoteFactory()
        String fullRepoName = 'davidsowerbykrail'

        when:
        factory.projectNameFromRemoteRepFullName(provider, fullRepoName)

        then:
        thrown GitPlusConfigurationException
    }
}
