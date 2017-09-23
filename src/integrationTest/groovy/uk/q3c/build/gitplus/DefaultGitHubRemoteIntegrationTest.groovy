package uk.q3c.build.gitplus

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import org.apache.commons.io.FileUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.guice.UseModules
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.DefaultGitPlus
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.local.PushResponse
import uk.q3c.build.gitplus.remote.GPIssue
import uk.q3c.build.gitplus.remote.GitRemoteException
import uk.q3c.build.gitplus.util.FilePropertiesLoader

import java.time.LocalDateTime

/**
 * This test needs to delete the 'dummy' repo in cleanup.  This test is a bit weird because it has to use deleteRepo to clean up, but also tests deleteRepo
 *
 * Created by David Sowerby on 20 Mar 2016
 */
@UseModules([GitPlusModule])
class DefaultGitHubRemoteIntegrationTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp


    @Inject
    GitPlus gitPlus

    GitPlus gitPlusForSetup


    def setup() {
        println 'cleaning up at start'

        if (remoteRepoExists('dummy')) {
            deleteRepo()
            waitRemoteRepoNotExists('dummy')
        }
        gitPlus.configuration.propertiesLoaders.add(new FilePropertiesLoader().sourceFromGradle())
        temp = temporaryFolder.getRoot()
    }


    def cleanup() {
        println 'cleaning up at end'
        if (remoteRepoExists('dummy')) {
            deleteRepo()
            waitRemoteRepoNotExists('dummy')
        }
    }


    def "lifecycle"() {
        given:
        gitPlus.createLocalAndRemote(temp, 'davidsowerby', 'dummy', true, true)
        gitPlus.remote.mergeIssueLabels = true

        when:
        gitPlus.execute()
        createTestIssues(10)
        createVersion('0.1')

        then:
        gitPlus.remote.getIssue(9)  // remote repo there, with issues created
        File localProjectDir = new File(temp, 'dummy')
        localProjectDir.exists() // local file copy
        File wikiLocalProjectDir = new File(temp, 'dummy.wiki')
        wikiLocalProjectDir.exists() // local file copy
        gitPlus.local.headDevelopCommitSHA() == gitPlus.remote.developHeadCommit()
        new File(gitPlus.local.projectDir(), 'README.md').exists()

        when:
        gitPlus.local.tag("forPush", "tag to be pushed")
        PushResponse r1 = gitPlus.local.pushTag("forPush")

        then: "gitHub implementation does not return anything useful for 'tags', so rely on no failure here"
        noExceptionThrown()
        r1.successful

        when:
        r1 = gitPlus.local.pushAllTags()

        then:
        r1.successful

        when:
        GitPlus gitplus2 = GitPlusFactory.instance
        gitplus2.useRemoteOnly('davidsowerby', 'dummy')
        gitplus2.configuration.propertiesLoaders.add(new FilePropertiesLoader().sourceFromGradle())
        gitplus2.execute()
        GPIssue issue9 = gitplus2.remote.getIssue(9)

        then: "is a valid issue"
        issue9.title == "Issue 9 Title"

        when: "issue number is invalid"
        GPIssue issue20 = gitplus2.remote.getIssue(20)

        then:
        thrown GitRemoteException

        when: "getIssue has invalid repo"
        gitplus2.remote.getIssue("davidsowerby", "krail-master", 315)

        then:
        thrown GitRemoteException
    }

    def "create remote only"() {
        given:
        gitPlus.createRemoteOnly('davidsowerby', 'dummy', true)
        LocalDateTime timeout = LocalDateTime.now().plusSeconds(5)

        when:
        gitPlus.execute()
        while (!remoteRepoExists('dummy') && LocalDateTime.now().isBefore(timeout)) {
            println 'waiting for remote repo to be created'
            Thread.sleep(1000)
        }
        createTestIssues(1)

        then:
        gitPlus.remote.getIssue(1)  // remote repo there, with issues created
    }

    def createTestIssues(int number) {
        List<String> labels = ImmutableList.of('bug', 'documentation', 'quality', 'bug', 'task', 'bug', 'performance', 'enhancement', 'task', 'bug')
        for (int i = 1; i <= number; i++) {
            gitPlus.remote.createIssue('Issue ' + i + ' Title', 'Some stuff about the issue', labels.get(i % 10))
        }

    }

    def createVersion(String version) {

        gitPlus.local.checkoutBranch(new GitBranch(DefaultGitPlus.DEVELOP_BRANCH))
        createFileAndAddToGit(1)
        gitPlus.local.commit('Fix #1 commit 1')
        modifyFile(1)
        gitPlus.local.commit('{{javadoc}}')
        createFileAndAddToGit(2)
        gitPlus.local.commit('Fix #2 commit 2')
        gitPlus.local.tag(version, 'version ' + version)
        gitPlus.local.push(true, false)
    }

    def modifyFile(int index) {
        File f = new File(gitPlus.local.projectDir(), index + '.txt')
        List<String> lines = FileUtils.readLines(f)
        lines.add('modified')
        FileUtils.writeLines(f, lines)
        gitPlus.local.add(f)
    }

    def createFileAndAddToGit(int index) {
        File f = new File(gitPlus.local.projectDir(), index + '.txt')
        List<String> lines = new ArrayList<>()
        lines.add('Test file')
        FileUtils.writeLines(f, lines)
        gitPlus.local.add(f)
    }

    /**
     * Delete repo using full access key
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    private void deleteRepo() {
        configureGitplusForSetup()
        gitPlusForSetup.remote.configuration.confirmDelete("I really, really want to delete the davidsowerby/dummy repo from GitHub")
        println 'deleting repo'
        gitPlusForSetup.execute()
        gitPlusForSetup.remote.deleteRepo()
    }


    @SuppressWarnings("GrMethodMayBeStatic")
    private boolean remoteRepoExists(String repoName) {
        configureGitplusForSetup()
        gitPlusForSetup.execute()
        println 'getting repo names'
        Set<String> names = gitPlusForSetup.remote.listRepositoryNames()
        println 'names retrieved'
        return names.contains(repoName)
    }

/**
 * returns as soon as it fails to find repoName (checking for absence / deletion).  Use waitRemoteRepoExists for fast return of found
 * @param repoName
 * @return
 */
    @SuppressWarnings("GrMethodMayBeStatic")
    private void waitRemoteRepoNotExists(String repoName) {
        println 'waiting for repo not to exist'

        configureGitplusForSetup()
        gitPlusForSetup.execute()

        def timeout = 20
        Set<String> names = gitPlusForSetup.remote.listRepositoryNames()
        while (names.contains(repoName) && timeout > 0) {
            println 'waiting 1 second for api, ' + timeout + ' before timeout'
            Thread.sleep(1000)
            timeout--
            names = gitPlusForSetup.remote.listRepositoryNames()
        }
        if (timeout <= 0) {
            throw new RuntimeException("Timed out")
        }
    }


    private configureGitplusForSetup() {
        gitPlusForSetup = GitPlusFactory.instance // we want fresh config every time
        gitPlusForSetup.remote.configuration
                .repoUser('davidsowerby')
                .repoName('dummy')
        gitPlusForSetup.configuration.propertiesLoaders.add(new FilePropertiesLoader().sourceFromGradle())
    }


}