package uk.q3c.build.gitplus

import com.google.inject.Inject
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.guice.UseModules
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.remote.github.GitHubRemote

/**
 * Created by David Sowerby on 11 Mar 2016
 */
@UseModules([GitPlusModule])
class DefaultGitHubRemoteTest2 extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    @Inject
    GitHubRemote remote

    @Inject
    GitPlus gitPlus

    def setup() {
        temp = temporaryFolder.getRoot()
    }

    def "latestCommit()"() {
        given:
        remote.repoUser = 'davidsowerby'
        remote.repoName = 'q3c-testUtil'

        when:
        String result1 = remote.latestCommitSHA(new GitBranch('develop'))
        String result2 = remote.latestDevelopCommitSHA()

        then:
        result1 != null
        result1 == result2
    }

    def "latest commit for local and remote, clone wiki"() {
        given:
        final String project = 'q3c-testUtil'
        gitPlus.local.projectName = project
        gitPlus.local.projectDirParent = temp
        gitPlus.local.cloneFromRemote(true)
        gitPlus.remote.repoUser('davidsowerby').repoName(project)
        gitPlus.wikiLocal.active = true
        gitPlus.wikiLocal.cloneFromRemote = true

        when:
        gitPlus.execute()
        String remote1 = gitPlus.remote.latestCommitSHA(new GitBranch('develop'))
        String remote2 = gitPlus.remote.latestDevelopCommitSHA()
        String local1 = gitPlus.local.latestCommitSHA(new GitBranch('develop'))
        String local2 = gitPlus.local.latestDevelopCommitSHA()

        then:
        remote1 == remote2
        remote1 == local1
        remote1 == local2
        gitPlus.wikiLocal.projectDir().exists()
        new File(gitPlus.wikiLocal.projectDir(), "Home.md").exists()
        new File(gitPlus.wikiLocal.projectDir(), ".git").exists()

    }

}