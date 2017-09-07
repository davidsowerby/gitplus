package uk.q3c.build.gitplus.remote

import spock.lang.Specification
/**
 * Created by David Sowerby on 18 Mar 2016
 */
class DefaultGitRemoteUrlMapperTest extends Specification {

    GitRemoteUrlMapper mapper
    GitRemoteConfiguration remoteConfiguration = new DefaultGitRemoteConfiguration()
    GitRemote remote = Mock(GitRemote)

    def setup() {
        remote.getConfiguration() >> remote
        mapper = new DefaultGitRemoteUrlMapper()
        remote.urlMapper >> mapper
        remote.providerBaseUrl >> 'github.com'
        remoteConfiguration.repoUser >> 'davidsowerby'
        remoteConfiguration.repoName >> 'krail'
        mapper.owner = remote

    }


    def "Urls with GitHub (default)"() {
        given:
        remote.remoteRepoFullName() >> 'davidsowerby/krail'
        final String cloneUrl = 'https://github.com/davidsowerby/krail.git'
        final String apiUrl = 'https://api.github.com'
        final String repoBaseUrl = 'https://github.com/davidsowerby/krail'
        final String tagUrl = 'https://github.com/davidsowerby/krail/tree/'
        final String issuesUrl = 'https://github.com/davidsowerby/krail/issues/'
        final String wikiUrl = 'https://github.com/davidsowerby/krail/wiki'
        final String wikiCloneUrl = 'https://github.com/davidsowerby/krail.wiki.git'


        expect:
        mapper.apiUrl() == apiUrl
        mapper.repoBaselUrl() == repoBaseUrl
        mapper.issuesUrl() == issuesUrl
        mapper.tagUrl() == tagUrl
        mapper.wikiCloneUrl() == wikiCloneUrl
        mapper.wikiUrl() == wikiUrl
        mapper.cloneUrl() == cloneUrl
    }


}
