package uk.q3c.gitplus.remote

import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
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
        final String USER = 'davidsowerby'
        final String cloneUrl = 'https://github.com/davidsowerby/krail.git'
        final String htmlUrlStem = 'https://github.com'
        final String apiUrlStem = 'https://api.github.com'
        final String htmlUrl = 'https://github.com/davidsowerby/krail'
        final String htmlTagUrl = 'https://github.com/davidsowerby/krail/tree'
        final String repoName = 'krail'
        final String fullRepoName = 'davidsowerby/krail'
        final String wikiHtmlUrl = 'https://github.com/davidsowerby/krail/wiki'
        final String wikiCloneUrl = 'https://github.com/davidsowerby/krail.wiki.git'

        GitRemoteFactory factory = new DefaultGitRemoteFactory()

        expect:
        factory.htmlUrlStem().equals(htmlUrlStem)
        factory.apiUrlStem().equals(apiUrlStem)
        factory.htmlUrlFromRepoName(USER, repoName).equals(htmlUrl)
        factory.htmlTagUrlFromFullRepoName(USER, repoName).equals(htmlTagUrl)
        factory.fullRepoNameFromHtmlUrl(htmlUrl).equals(fullRepoName)
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


}
