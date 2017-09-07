package uk.q3c.build.gitplus

import com.google.inject.Inject
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.guice.UseModules
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.local.GitLocalException

/**
 * Created by David Sowerby on 11 Mar 2016
 */
@UseModules([GitPlusModule])
class DefaultGitHubRemoteTest2 extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    @Inject
    GitPlus gitPlus

    final String projectName = 'q3c-testutils'
    final String remoteUser = 'davidsowerby'

    def setup() {
        temp = temporaryFolder.getRoot()
        gitPlus.remote.repoUser = remoteUser
        gitPlus.remote.repoName = projectName
        gitPlus.propertiesFromGradle()
    }

    def "headCommit()"() {
        given:
        gitPlus.execute()

        when:
        String result1 = gitPlus.remote.headCommit(new GitBranch('develop'))
        String result2 = gitPlus.remote.developHeadCommit()

        then:
        result1 != null
        result1 == result2
    }

    def "head commit for local and remote, clone wiki"() {
        given:

        gitPlus.cloneFromRemote(temp, remoteUser, projectName, true)
        gitPlus.execute()

        when:
        gitPlus.local.checkoutRemoteBranch(new GitBranch("develop"))
        String remote1 = gitPlus.remote.headCommit(new GitBranch('develop'))
        String remote2 = gitPlus.remote.developHeadCommit()
        String local1 = gitPlus.local.headCommitSHA(new GitBranch('develop'))
        String local2 = gitPlus.local.headDevelopCommitSHA()

        then:
        remote1 == remote2
        remote1 == local1
        remote1 == local2
        gitPlus.wikiLocal.projectDir().exists()
        new File(gitPlus.wikiLocal.projectDir(), "Home.md").exists()
        new File(gitPlus.wikiLocal.projectDir(), ".git").exists()

        when: "non-existent branch requested"
        gitPlus.local.headCommitSHA(new GitBranch('rubbish'))

        then:
        thrown GitLocalException

    }

}