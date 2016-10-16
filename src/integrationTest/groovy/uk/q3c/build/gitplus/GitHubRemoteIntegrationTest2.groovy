package uk.q3c.build.gitplus

import com.google.inject.Inject
import spock.guice.UseModules
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.remote.github.DefaultGitHubRemote

/**
 *
 * Created by David Sowerby on 20 Mar 2016
 */
@UseModules([GitPlusModule])
class GitHubRemoteIntegrationTest2 extends Specification {


    @Inject
    GitPlus gitPlus

    def setup() {
        gitPlus.remote.repoUser('davidsowerby').repoName('krail')
    }

    def "api status"() {

        expect:
        gitPlus.remote.apiStatus() == DefaultGitHubRemote.Status.GREEN
    }

    def "list repos for this user"() {

        when:
        gitPlus.remote.repoUser = 'davidsowerby'
        Set<String> repos = gitPlus.remote.listRepositoryNames()

        then:
        repos.contains('krail')
        !repos.contains('perl')
        repos.size() >= 17

    }


}