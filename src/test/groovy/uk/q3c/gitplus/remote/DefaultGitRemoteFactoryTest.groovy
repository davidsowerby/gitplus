package uk.q3c.gitplus.remote

import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.gitplus.GitPlusConfigurationException

/**
 * Created by David Sowerby on 18 Mar 2016
 */
class DefaultGitRemoteFactoryTest extends Specification {

    GitPlusConfiguration gitPlusConfiguration
    GitRemote.ServiceProvider provider = GitRemote.ServiceProvider.GITHUB

    def setup() {
        gitPlusConfiguration = new GitPlusConfiguration()
    }

    def "create"() {
        expect:
        new DefaultGitRemoteFactory().createRemoteInstance(gitPlusConfiguration) instanceof GitHubRemote
    }

    def "Urls"() {
        given:
        final String cloneUrl = 'https://github.com/davidsowerby/krail.git'
        final String htmlUrlStem = 'https://github.com'
        final String apiUrlStem = 'https://api.github.com'
        final String htmlUrl = 'https://github.com/davidsowerby/krail'
        final String htmlTagUrl = 'https://github.com/davidsowerby/krail/tree'
        final String fullRepoName = 'davidsowerby/krail'
        final String projectName = 'krail'
        final String wikiHtmlUrl = 'https://github.com/davidsowerby/krail/wiki'
        final String wikiCloneUrl = 'https://github.com/davidsowerby/krail.wiki.git'

        GitRemoteFactory factory = new DefaultGitRemoteFactory()

        expect:
        factory.htmlUrlStem().equals(htmlUrlStem)
        factory.apiUrlStem().equals(apiUrlStem)
        factory.cloneUrlFromFullRepoName(fullRepoName).equals(cloneUrl)
        factory.htmlUrlFromFullRepoName(fullRepoName).equals(htmlUrl)
        factory.htmlTagUrlFromFullRepoName(fullRepoName).equals(htmlTagUrl)
        factory.fullRepoNameFromHtmlUrl(fullRepoName).equals(fullRepoName)
        factory.projectNameFromFullRepoName(fullRepoName).equals(projectName)
        factory.htmlUrlFromCloneUrl(cloneUrl).equals(htmlUrl)
        factory.fullRepoNameFromCloneUrl(cloneUrl).equals(fullRepoName)
        factory.wikiHtmlUrlFromCoreHtmlUrl(htmlUrl).equals(wikiHtmlUrl)
        factory.wikiCloneUrlFromCoreHtmLUrl(htmlUrl).equals(wikiCloneUrl)
        factory.cloneUrlFromHtmlUrl(htmlUrl).equals(cloneUrl)
    }

    def "set and get provider"() {
        given:
        GitRemoteFactory factory = new DefaultGitRemoteFactory()

        when:
        factory.setRemoteServiceProvider(GitRemote.ServiceProvider.GITHUB)

        then:
        factory.getRemoteServiceProvider() == GitRemote.ServiceProvider.GITHUB
    }

    def "malformed full repo name throws NPE"() {
        given:
        GitRemoteFactory factory = new DefaultGitRemoteFactory()
        String fullRepoName = 'davidsowerbykrail'

        when:
        factory.projectNameFromFullRepoName(fullRepoName)

        then:
        thrown GitPlusConfigurationException
    }
}
