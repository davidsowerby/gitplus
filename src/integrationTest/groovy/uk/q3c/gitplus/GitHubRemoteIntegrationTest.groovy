package uk.q3c.gitplus

import com.google.common.collect.ImmutableList
import org.apache.commons.io.FileUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.gitplus.changelog.ChangeLog
import uk.q3c.gitplus.changelog.ChangeLogConfiguration
import uk.q3c.gitplus.gitplus.GitPlus
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.local.GitLocal
import uk.q3c.gitplus.local.GitLocalProvider
import uk.q3c.gitplus.remote.GitHubProvider
import uk.q3c.gitplus.remote.GitHubRemote
import uk.q3c.gitplus.remote.GitRemote
import uk.q3c.gitplus.remote.RemoteRequest
import uk.q3c.util.testutil.FileTestUtil

import java.nio.file.Paths

/**
 * This test needs to delete the 'dummy' repo in cleanup.  This test is a bit weird because it has to use deleteRepo to clean up, but also tests deleteRepo
 *
 * Created by David Sowerby on 20 Mar 2016
 */
class GitHubRemoteIntegrationTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    GitPlusConfiguration gitPlusConfiguration
    GitRemote gitRemote
    GitPlus gitPlus
    GitLocalProvider gitLocalProvider
    GitLocal gitLocal

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
        gitPlusConfiguration = new GitPlusConfiguration()
                .remoteRepoFullName('davidsowerby/dummy')
                .createLocalRepo(true)
                .createRemoteRepo(true)
                .publicProject(true)
                .projectDirParent(temp)
                .useWiki(true)
        gitLocalProvider = new GitLocalProvider()
        gitPlus = new GitPlus(gitPlusConfiguration, gitLocalProvider)
        gitLocal = gitPlus.getGitLocal()
        ChangeLogConfiguration changeLogConfiguration = new ChangeLogConfiguration()
        gitRemote = gitPlus.getGitRemote()

        URL url = this.getClass()
                .getResource('changelog-step1.md');
        File expectedResult1 = Paths.get(url.toURI())
                .toFile();

        when:
        gitPlus.createOrVerifyRepos()
        createTestIssues(10)
        createVersion('0.1')
        ChangeLog changeLog = generateChangeLog(changeLogConfiguration)

        then:
        FileTestUtil.compare(changeLog.getOutputFile(), expectedResult1)
        gitPlus.getWikiLocal().getProjectDir().exists()
        new File(gitPlus.getWikiLocal().getProjectDir(), '.git').exists()
        new File(gitPlus.getWikiLocal().getProjectDir(), 'changelog.md').exists()

    }

    def ChangeLog generateChangeLog(ChangeLogConfiguration changeLogConfiguration) {
        ChangeLog changeLog = new ChangeLog(gitPlus, changeLogConfiguration)
        changeLog.createChangeLog()
        return changeLog
    }

    def createTestIssues(int number) {
        List<String> labels = ImmutableList.of('bug', 'documentation', 'quality', 'bug', 'task', 'bug', 'performance', 'enhancement', 'task', 'bug')
        for (int i = 0; i < number; i++) {
            gitRemote.createIssue('Issue ' + i + ' Title', 'Some stuff about the issue', labels.get(i % 10))
        }

    }

    def createVersion(String version) {

        gitLocal.checkout(GitPlus.DEVELOP_BRANCH)
        createFileAndAddToGit(1)
        gitLocal.commit('Fix #1 commit 1')
        modifyFile(1)
        gitLocal.commit('{{javadoc}}')
        createFileAndAddToGit(2)
        gitLocal.commit('Fix #2 commit 2')
        gitLocal.tag(version)
        gitLocal.push(gitRemote, true)
    }

    def modifyFile(int index) {
        File f = new File(gitPlusConfiguration.getProjectDir(), index + '.txt')
        List<String> lines = FileUtils.readLines(f)
        lines.add('modified')
        FileUtils.writeLines(f, lines)
        gitLocal.add(f)
    }

    def createFileAndAddToGit(int index) {
        File f = new File(gitPlusConfiguration.getProjectDir(), index + '.txt')
        List<String> lines = new ArrayList<>()
        lines.add('Test file')
        FileUtils.writeLines(f, lines)
        gitLocal.add(f)
    }

    /**
     * Delete repo using full access key
     */
    private void deleteRepo() {
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration().remoteRepoFullName('davidsowerby/dummy')
                .confirmRemoteDelete("I really, really want to delete the davidsowerby/dummy repo from GitHub")
        GitRemote remote = new GitHubRemote(dummyConfiguration, new GitHubProvider(), new RemoteRequest())
        println 'deleting repo'
        remote.deleteRepo()
    }

/**
 * returns as soon as it finds repoName.  Use waitRemoteRepoNotExists for fast return of not found
 * @param repoName
 * @return
 */
    private void waitRemoteRepoExists(String repoName) {
        println 'waiting for repo to exist'
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration().remoteRepoFullName('davidsowerby/dummy')
        GitRemote remote = new GitHubRemote(dummyConfiguration, new GitHubProvider(), new RemoteRequest())
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

    private boolean remoteRepoExists(String repoName) {
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration().remoteRepoFullName('davidsowerby/dummy')
        GitRemote remote = new GitHubRemote(dummyConfiguration, new GitHubProvider(), new RemoteRequest())
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
    private void waitRemoteRepoNotExists(String repoName) {
        println 'waiting for repo not to exist'
        GitPlusConfiguration dummyConfiguration = new GitPlusConfiguration().remoteRepoFullName('davidsowerby/dummy')
        GitRemote remote = new GitHubRemote(dummyConfiguration, new GitHubProvider(), new RemoteRequest())
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