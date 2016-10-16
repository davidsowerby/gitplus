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
import uk.q3c.build.gitplus.remote.DefaultGitRemoteConfiguration
import uk.q3c.build.gitplus.remote.DefaultRemoteRequest
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.github.DefaultGitHubProvider
import uk.q3c.build.gitplus.remote.github.DefaultGitHubRemote
import uk.q3c.build.gitplus.remote.github.GitHubUrlMapper
import uk.q3c.build.gitplus.util.FileBuildPropertiesLoader

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


    def setup() {
        println 'cleaning up at start'
        if (remoteRepoExists('dummy')) {
            deleteRepo()
            waitRemoteRepoNotExists('dummy')
        }
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

        gitPlus.remote.repoUser('davidsowerby').repoName('dummy').create(true).publicProject(true).mergeIssueLabels(true)
        gitPlus.local.create(true).projectDirParent(temp).projectName('dummy')
        gitPlus.wikiLocal.active(true)
        gitPlus.wikiLocal.create(true)
//        URL url = this.getClass()
//                .getResource('changelog-step1.md');
//        File expectedResult1 = Paths.get(url.toURI())
//                .toFile();

        when:
        gitPlus.execute()
//        createTestIssues(10)
        createVersion('0.1')

        then:
//        gitPlus.remote.getIssue(9)  // remote repo there, with issues created
        File localFile = new File(temp, 'dummy')
        localFile.exists() // local file copy
        File wikiLocalFile = new File(temp, 'dummy.wiki')
        wikiLocalFile.exists() // local file copy
        gitPlus.local.latestDevelopCommitSHA() == gitPlus.remote.latestDevelopCommitSHA()
    }


    def createTestIssues(int number) {
        List<String> labels = ImmutableList.of('bug', 'documentation', 'quality', 'bug', 'task', 'bug', 'performance', 'enhancement', 'task', 'bug')
        for (int i = 0; i < number; i++) {
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
        gitPlus.local.tag(version)
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
        DefaultGitRemoteConfiguration dummyConfiguration = new DefaultGitRemoteConfiguration()
        dummyConfiguration.repoUser('davidsowerby').repoName('dummy').confirmDelete("I really, really want to delete the davidsowerby/dummy repo from GitHub")
        GitRemote remote = new DefaultGitHubRemote(dummyConfiguration, new DefaultGitHubProvider(new FileBuildPropertiesLoader()), new DefaultRemoteRequest(), new GitHubUrlMapper())
        println 'deleting repo'
        remote.deleteRepo()
    }

/**
 * returns as soon as it finds repoName.  Use waitRemoteRepoNotExists for fast return of not found
 * @param repoName
 * @return
 */
    @SuppressWarnings("GrMethodMayBeStatic")
    private void waitRemoteRepoExists(String repoName) {
        println 'waiting for repo to exist'
        DefaultGitRemoteConfiguration dummyConfiguration = new DefaultGitRemoteConfiguration()
        dummyConfiguration.repoUser('davidsowerby').repoName('dummy').confirmDelete("I really, really want to delete the davidsowerby/dummy repo from GitHub")
        GitRemote remote = new DefaultGitHubRemote(dummyConfiguration, new DefaultGitHubProvider(new FileBuildPropertiesLoader()), new DefaultRemoteRequest(), new GitHubUrlMapper())
        def timeout = 20
        Set<String> names = remote.listRepositoryNames()
        while (!names.contains(repoName) && timeout > 0) {
            println 'waiting 1 second for api, ' + timeout + ' before timeout'
            Thread.sleep(1000)
            timeout--
            names = remote.listRepositoryNames()
        }
        if (timeout <= 0) {
            throw new RuntimeException("Timed out")
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private boolean remoteRepoExists(String repoName) {
        DefaultGitRemoteConfiguration dummyConfiguration = new DefaultGitRemoteConfiguration()
        dummyConfiguration.repoUser('davidsowerby').repoName('dummy')
        GitRemote remote = new DefaultGitHubRemote(dummyConfiguration, new DefaultGitHubProvider(new FileBuildPropertiesLoader()), new DefaultRemoteRequest(), new GitHubUrlMapper())
        println 'getting repo names'
        Set<String> names = remote.listRepositoryNames()
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
        DefaultGitRemoteConfiguration dummyConfiguration = new DefaultGitRemoteConfiguration()
        dummyConfiguration.repoUser('davidsowerby').repoName('dummy')
        GitRemote remote = new DefaultGitHubRemote(dummyConfiguration, new DefaultGitHubProvider(new FileBuildPropertiesLoader()), new DefaultRemoteRequest(), new GitHubUrlMapper())
        def timeout = 20
        Set<String> names = remote.listRepositoryNames()
        while (names.contains(repoName) && timeout > 0) {
            println 'waiting 1 second for api, ' + timeout + ' before timeout'
            Thread.sleep(1000)
            timeout--
            names = remote.listRepositoryNames()
        }
        if (timeout <= 0) {
            throw new RuntimeException("Timed out")
        }
    }


}