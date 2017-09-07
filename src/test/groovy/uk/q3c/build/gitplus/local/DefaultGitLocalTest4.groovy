package uk.q3c.build.gitplus.local

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import org.eclipse.jgit.api.*
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.lib.BranchConfig
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.StoredConfig
import org.eclipse.jgit.merge.MergeStrategy
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.GitSHA
import uk.q3c.build.gitplus.gitplus.DefaultGitPlus
import uk.q3c.build.gitplus.gitplus.FileDeleteApprover
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.test.MocksKt
import uk.q3c.build.gitplus.util.PropertiesResolver

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

/**
 *
 * These test use Mockito rather than Spock's own mocks
 *
 * Created by David Sowerby on 17 Mar 2016
 */
class DefaultGitLocalTest4 extends Specification {


    class TestDirDeleteApprover implements FileDeleteApprover {

        File tempfile

        TestDirDeleteApprover(File tempfile) {
            this.tempfile = tempfile
        }

        @Override
        boolean approve(File file) {
            return file.getParentFile().equals(tempfile)
        }
    }

    class TestDirDeleteDenier implements FileDeleteApprover {

        @Override
        boolean approve(File file) {
            return false
        }
    }


    @Rule
    TemporaryFolder temporaryFolder

    File temp

    GitLocal gitLocal
    GitRemote gitRemote
    PropertiesResolver apiPropertiesResolver = mock(PropertiesResolver)
    GitLocalConfiguration configuration
    Git mockGit

    GitProvider gitProvider
    GitProvider mockGitProvider = mock(GitProvider)
    Repository repo
    GitInitChecker initChecker = new DefaultGitInitChecker()
    GitInitChecker mockInitChecker = mock(GitInitChecker)
    GitCloner cloner


    BranchConfigProvider branchConfigProvider = mock(BranchConfigProvider)
    BranchConfig branchConfig = mock(BranchConfig)
    ProjectCreator mockProjectCreator = mock(ProjectCreator)
    String baseUrl = "repo/base"
    String wikiUrl = "repo/base/wiki"
    GitPlus gitPlus

    def setup() {
        cloner = new MockGitCloner()
        gitProvider = new DefaultGitProvider()

        mockGit = MocksKt.mockGit()
        repo = mockGit.repository
        when(mockGitProvider.openRepository(configuration)).thenReturn(mockGit)
        temp = temporaryFolder.getRoot()
        gitPlus = MocksKt.mockGitPlusWithDataConfig()
        gitRemote = gitPlus.remote
        configuration = gitPlus.local.configuration

    }

    def "construct with null configuration throws IllegalArgumentException"() {
        when:
        gitLocal = new DefaultGitLocal(branchConfigProvider, gitProvider, null, mockInitChecker, cloner)

        then:
        thrown(IllegalArgumentException)
    }

    def "null gitProvider causes IllegalArgumentException in constructor"() {
        when:
        new DefaultGitLocal(branchConfigProvider, null, configuration, mockInitChecker, cloner)

        then:
        thrown IllegalArgumentException
    }

    def "init prepares directory, invokes projectCreator and adds projectDir to Git"() {
        given:
        configuration.projectDirParent(temp).projectCreator(mockProjectCreator)
        gitLocal = createGitLocal(false, false, false)


        when:
        gitLocal.init()

        then:
        File projectDir = new File(temp, 'wiggly')
        new File(projectDir, ".git").exists()
        verify(mockProjectCreator, times(1)).invoke(configuration)
        gitLocal.status().isClean()
        gitLocal.initDone
    }


    def "create local repo"() {
        given:
        configuration.create(true).projectDirParent(temp)
        gitLocal = createGitLocal(false, false, false)

        when:
        gitLocal.createAndInitialise()

        then:
        new File(temp, "wiggly/.git").exists()
        gitLocal.initDone
    }

    def "set and get origin, sets to remote"() {
        given:
        String remoteCloneUrl = 'https://remoteUrl'
        when(gitRemote.cloneUrl()).thenReturn(remoteCloneUrl)
        configuration.create(true).projectDirParent(temp).projectName('dummy')
        gitLocal = createGitLocal(false, false, false)


        when:
        gitLocal.createAndInitialise()
        gitLocal.setOrigin()

        then:
        gitLocal.getOrigin() == remoteCloneUrl
    }

    def "set origin, git throws exception"() {
        given:
        gitRemote.cloneUrl() >> { throw new NullPointerException() }
        mockGit.getRepository() >> { throw new IOException() }
        configuration.create(true).projectDirParent(temp).projectName('dummy')
        gitLocal = createGitLocal(false, false, false)



        when:
        gitLocal.setOrigin()

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Unable to set origin")

    }

    def "getOrigin has no origin defined"() {

        given:
        Repository repository = mock(Repository)
        when(mockGit.repository).thenReturn(repository)
        StoredConfig storedConfig = mock(StoredConfig)
        when(repository.config).thenReturn(storedConfig)
//        when(gitRemote.repoName).thenReturn('wiggly')
        println gitPlus.remote.repoName
        when(storedConfig.getSubsections(DefaultGitPlus.REMOTE)).thenReturn(ImmutableSet.of("a"))
        when(mockGitProvider.openRepository(any(GitLocalConfiguration))).thenReturn(mockGit)
        gitLocal = createGitLocal(true, true, true)


        when: "there is no origin section in stored config"
        gitLocal.getOrigin()

        then: "throws exception"
        GitLocalException e = thrown()
        e.message == "Unable to get the origin"
    }

    def "retrieving origin throws exception, catch & throw GitLocalException"() {

        given:
        when(mockGitProvider.openRepository(any(GitLocalConfiguration))).thenReturn(mockGit)
        gitLocal = createGitLocal(true, true, true)
        StoredConfig storedConfig = mockGit.repository.config
        when(storedConfig.getSubsections(DefaultGitPlus.REMOTE)).thenThrow(new NullPointerException())
        when(gitRemote.repoName).thenReturn("wiggly")

        when: "there is no origin section in stored config"
        gitLocal.getOrigin()

        then: "throws exception"
        GitLocalException e = thrown()
        e.message == "Unable to get the origin"
    }

    def "set origin from remote"() {
        given:
        when(gitRemote.repoUser).thenReturn('davidsowerby')
        when(gitRemote.repoName).thenReturn('scratch')
        final String remoteUrl = 'https://github.com/davidsowerby/scratch.git'
        when(gitRemote.cloneUrl()).thenReturn(remoteUrl)
        gitLocal = createGitLocal(false, false, false)
        configuration.create(true).projectDirParent(temp).projectName('dummy')

        when:
        gitLocal.createAndInitialise()
        gitLocal.setOrigin()

        then:
        gitLocal.getOrigin() == remoteUrl
    }

    def "create local repo failure throws GitLocalException"() {
        given:
        configuration.create = true
        configuration.projectDirParent = temp
        when(mockGit.repository).thenReturn(repo)
        when(repo.directory).thenReturn(temp)
        when(mockGitProvider.openRepository(any(GitLocalConfiguration))).thenReturn(mockGit)
        when(mockInitChecker.initDone).thenThrow(new NullPointerException())
        gitLocal = createGitLocal(true, false, false)


        when:
        gitLocal.createAndInitialise()

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Unable to create local repo")
    }


    def "close() .. then call getGit() returns same git instance"() {
        given:
        createScratchRepo()
        Git firstRef = gitLocal.getGit()

        when:
        gitLocal.close()

        then:
        gitLocal.getGit() == firstRef

    }

    def "call close()twice does not cause NPE"() {
        given:
        createScratchRepo()
        gitLocal.getGit()

        when:
        gitLocal.close()
        gitLocal.close()

        then:
        noExceptionThrown()
    }


    def "push with null branch throws GLE"() {
        given:
        when(mockGitProvider.openRepository(any(GitLocalConfiguration))).thenReturn(mockGit)
        gitLocal = createGitLocal(true, true, true)
        when(repo.getBranch()).thenReturn(null)

        when:
        gitLocal.push(false, false)

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Unable to push")
        gle.cause instanceof GitLocalException
        gle.cause.message.contains("Unable to get current branch")
    }

    def "push with all valid is successful"() {
        given:
        when(mockGitProvider.openRepository(any(GitLocalConfiguration))).thenReturn(mockGit)
        gitLocal = createGitLocal(true, true, true)
        CredentialsProvider credentialsProvider = mock(CredentialsProvider)
        when(gitRemote.getCredentialsProvider()).thenReturn(credentialsProvider)
        PushCommand pc = mock(PushCommand)
        when(repo.isBare()).thenReturn(false)
        when(repo.getBranch()).thenReturn('develop')
        when(gitRemote.active).thenReturn(false)
        when(mockGit.push()).thenReturn(pc)
        when(pc.add('develop')).thenReturn(pc)
        when(pc.setCredentialsProvider(credentialsProvider)).thenReturn(pc)


        PushResult pushResult1 = mock(PushResult)
        PushResult pushResult2 = mock(PushResult)
        RemoteRefUpdate update1 = mock(RemoteRefUpdate)
        RemoteRefUpdate update2 = mock(RemoteRefUpdate)
        RemoteRefUpdate update3 = mock(RemoteRefUpdate)
        RemoteRefUpdate update4 = mock(RemoteRefUpdate)
        List<RemoteRefUpdate> updates1 = ImmutableList.of(update1)
        List<RemoteRefUpdate> updates2 = ImmutableList.of(update2, update3, update4)
        Iterable<PushResult> results = ImmutableList.of(pushResult1, pushResult2)
        when(pushResult1.getRemoteUpdates()).thenReturn(updates1)
        when(pushResult2.getRemoteUpdates()).thenReturn(updates2)
        when(pc.call()).thenReturn(results)
        when(update1.status).thenReturn(RemoteRefUpdate.Status.OK)


        when:
        PushResponse response = gitLocal.push(true, true)

        then:
        verify(pc, times(1)).setPushTags() == null
        verify(pc).setForce(true) == null
        response.getUpdates().size() == 4
        response.getUpdates().containsAll(ImmutableList.of(update1, update2, update3, update4))

    }


    def "push with result failure throws PushException wrapped in GLE"() {
        given:
        when(mockGitProvider.openRepository(any(GitLocalConfiguration))).thenReturn(mockGit)
        gitLocal = createGitLocal(true, true, true)
        CredentialsProvider credentialsProvider = Mock(CredentialsProvider)
        gitRemote.getCredentialsProvider() >> credentialsProvider
        when(repo.isBare()).thenReturn(false)
        when(repo.getBranch()).thenReturn('develop')
        when(gitRemote.active).thenReturn(false)

        PushCommand pc = mock(PushCommand)
        when(mockGit.push()).thenReturn(pc)
        when(pc.add('develop')).thenReturn(pc)
        when(pc.setCredentialsProvider(credentialsProvider)).thenReturn(pc)



        PushResult pushResult1 = mock(PushResult)
        PushResult pushResult2 = mock(PushResult)
        RemoteRefUpdate update1 = mock(RemoteRefUpdate)
        RemoteRefUpdate update2 = mock(RemoteRefUpdate)
        RemoteRefUpdate update3 = mock(RemoteRefUpdate)
        RemoteRefUpdate update4 = mock(RemoteRefUpdate)
        List<RemoteRefUpdate> updates1 = ImmutableList.of(update1)
        List<RemoteRefUpdate> updates2 = ImmutableList.of(update2, update3, update4)
        Iterable<PushResult> results = ImmutableList.of(pushResult1, pushResult2)
        when(pushResult1.getRemoteUpdates()).thenReturn(updates1)
        when(pushResult2.getRemoteUpdates()).thenReturn(updates2)
        when(pc.call()).thenReturn(results)
        when(update1.status).thenReturn(RemoteRefUpdate.Status.REJECTED_NODELETE)

        when:
        PushResponse response = gitLocal.push(false, false)

        then:
        verify(pc, never()).setPushTags() == null
        verify(pc).setForce(false) == null

        GitLocalException gle = thrown()
        gle.message.contains("Unable to push")
        gle.cause instanceof PushException

    }

    def "add file to git, commit, branch and checkout"() {
        given:
        createScratchRepo()
        File f = createAFile()

        when:
        gitLocal.add(f)

        then:
        gitLocal.status().getAdded().contains("test.md")
        gitLocal.status().hasUncommittedChanges()

        when:
        gitLocal.commit("wiggly")

        then:
        gitLocal.status().getAdded().isEmpty()
        !gitLocal.status().hasUncommittedChanges()

        when:
        gitLocal.createBranch("develop")

        then:
        gitLocal.branches().contains("develop")

        when:
        gitLocal.checkoutBranch(new GitBranch("develop"))

        then:
        gitLocal.currentBranch().equals(new GitBranch("develop"))

        when:
        gitLocal.checkoutBranch(new GitBranch("branch"))

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Unable to checkout branch branch")
    }

    def "mergeBranch"() {
        given:
        createScratchRepo()
        GitBranch masterBranch = gitLocal.masterBranch()
        GitBranch simplycdBranch = new GitBranch("simplycd")
        File f1 = createFileAndAddToGit()
        gitLocal.commit("committed to master")
        gitLocal.checkoutNewBranch(simplycdBranch)
        File f2 = createFileAndAddToGit()
        gitLocal.commit("committed to simplycd")

        when:
        gitLocal.checkoutBranch(masterBranch)
        MergeResult result = gitLocal.mergeBranch(simplycdBranch, MergeStrategy.THEIRS, MergeCommand.FastForwardMode.FF)

        then:
        noExceptionThrown()
        result.getMergeStatus().isSuccessful()
    }

    def "mergeBranch is not successful"() {
        given:
        Ref simplyCdRef = Mock(Ref)
        when(repo.findRef("simplycd")).thenReturn(simplyCdRef)
        MergeCommand mergeCommand = mock(MergeCommand)
        when(mockGit.merge()).thenReturn(mergeCommand)
        MergeResult mergeResult = mock(MergeResult)
        when(mergeCommand.call()).thenReturn(mergeResult)
        MergeResult.MergeStatus mergeStatus = mock(MergeResult.MergeStatus)
        when(mergeResult.getMergeStatus()).thenReturn(mergeStatus)
        when(mergeResult.toString()).thenReturn("totally banjaxed")
        when(mergeStatus.isSuccessful()).thenReturn(false)
        when(mockGitProvider.openRepository(any(GitLocalConfiguration))).thenReturn(mockGit)
        gitLocal = createGitLocal(true, true, true)
        GitBranch simplycdBranch = new GitBranch("simplycd")

        when:
        gitLocal.mergeBranch(simplycdBranch, MergeStrategy.THEIRS, MergeCommand.FastForwardMode.FF)

        then:
        GitLocalException ex = thrown GitLocalException
        ex.message.contains("totally banjaxed")
        ex.message.contains("Merge unsuccessful")
    }

    def "mergeBranch throws exception"() {
        given:
        MergeCommand mergeCommand = mock(MergeCommand)
        when(mockGit.merge()).thenReturn(mergeCommand)
        when(mergeCommand.call()).thenThrow(new NoHeadException("Fake exception"))
        when(mockGitProvider.openRepository(any(GitLocalConfiguration))).thenReturn(mockGit)
        gitLocal = createGitLocal(true, true, true)
        GitBranch simplycdBranch = new GitBranch("simplycd")

        when:
        gitLocal.mergeBranch(simplycdBranch, MergeStrategy.THEIRS, MergeCommand.FastForwardMode.FF)

        then:
        GitLocalException ex = thrown GitLocalException
        ex.message.contains("Exception thrown during merge")
    }

/**
 * Uses existing project as older commits will not then be subject to change
 */
    def "checkout specific commit"() {

        given:
        final String testProject = 'q3c-testutils'
        when(gitRemote.repoUser).thenReturn('davidsowerby')
        when(gitRemote.repoName).thenReturn(testProject)
        when(gitRemote.repoBaselUrl()).thenReturn('https://github.com/davidsowerby/' + testProject)
        when(gitRemote.cloneUrl()).thenReturn('https://github.com/davidsowerby/' + testProject + ".git")
        gitLocal = createGitLocal(false, false, false)
        configuration.projectName(testProject).projectDirParent(temp).cloneFromRemote(true)
        String commitHash = 'bbc4185540916d9d5f1fb5114b9181f50d444c45'
        gitLocal.prepare(gitPlus)

        when:
        gitLocal.cloneRemote()
        gitLocal.checkoutCommit(new GitSHA(commitHash))

        then:
        gitLocal.currentCommitHash().equals(commitHash)


        when:
        gitLocal.checkoutNewBranch(new GitBranch('wiggly'))

        then:
        gitLocal.currentBranch().equals(new GitBranch('wiggly'))
        gitLocal.currentCommitHash().equals(commitHash)
    }

    def "checkout remote branch"() {
        given:
        final String testProject = 'q3c-testutils'
        when(gitRemote.repoUser).thenReturn('davidsowerby')
        when(gitRemote.repoName).thenReturn(testProject)
        when(gitRemote.repoBaselUrl()).thenReturn('https://github.com/davidsowerby/' + testProject)
        when(gitRemote.cloneUrl()).thenReturn('https://github.com/davidsowerby/' + testProject + ".git")
        gitLocal = createGitLocal(false, false, false)
        configuration.projectName(testProject).projectDirParent(temp).cloneFromRemote(true)
        gitLocal.prepare(gitPlus)

        when: "branch is valid"
        gitLocal.cloneRemote()
        gitLocal.checkoutRemoteBranch(new GitBranch("develop"))

        then: "is successful"
        gitLocal.currentBranch().equals(gitLocal.developBranch())

        when: "name does not exist remotely"
        gitLocal.checkoutRemoteBranch(new GitBranch("rubbish"))

        then: "exception thrown"
        thrown GitLocalException

        when: "branch already exists"
        gitLocal.checkoutRemoteBranch(new GitBranch("develop"))

        then: "exception thrown"
        thrown GitLocalException


    }
/**
 * Uses existing project as older commits will not then be subject to change
 */
    def "checkout specific commit using new branch"() {

        given:
        final String testProject = 'q3c-testutils'
        when(gitRemote.repoUser).thenReturn('davidsowerby')
        when(gitRemote.repoName).thenReturn(testProject)
        when(gitRemote.repoBaselUrl()).thenReturn('https://github.com/davidsowerby/' + testProject)
        when(gitRemote.cloneUrl()).thenReturn('https://github.com/davidsowerby/' + testProject + ".git")
        gitLocal = createGitLocal(false, false, false)
        configuration.projectName(testProject).projectDirParent(temp).cloneFromRemote(true)
        String commitHash = 'bbc4185540916d9d5f1fb5114b9181f50d444c45'
        gitLocal.prepare(gitPlus)

        when:
        gitLocal.cloneRemote()

        then:
        gitLocal.currentBranch().equals(gitLocal.masterBranch())

        when:
        gitLocal.checkoutCommit(new GitSHA(commitHash), "temptest")

        then:
        gitLocal.currentCommitHash().equals(commitHash)
        gitLocal.currentBranch().name == "temptest"
    }


    def "checkoutNewBranch or checkoutCommit failure causes GLE"() {
        given:
        CheckoutCommand checkout = Mock(CheckoutCommand)
        mockGit.checkout() >> checkout
        mockGit.repository >> repo
        checkout.call() >> { throw new IOException() }
        gitRemote.active >> false
        when(mockGitProvider.openRepository(any(GitLocalConfiguration))).thenReturn(mockGit)
        gitLocal = createGitLocal(true, true, true)
        gitLocal.prepare(gitPlus)


        when:
        gitLocal.checkoutNewBranch(new GitBranch('wiggly'))

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Unable to checkout branch wiggly")

        when:
        gitLocal.checkoutCommit(new GitSHA('0123456701234567012345670123456701234567'))

        then:
        gle = thrown()
        gle.message.contains("Unable to checkout commit 0123456701234567012345670123456701234567")


    }

    def "GitLocalException when init call fails"() {
        given:
        gitLocal = createGitLocal(false, false, false)
        gitLocal.projectName('scratch').projectDirParent(temp)
        temp.writable = false  // to force error

        when:
        gitLocal.init()

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Unable to initialise DefaultGitLocal")

    }

    def "GitLocalException when adding a non-existent file"() {
        given:
        configuration.create(true).projectName("scratch").projectDirParent(temp)
        gitLocal = createGitLocal(false, true, true)

        when:

        gitLocal.add(new File(temp, "test.md"))

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Unable to add file to git")
        gle.cause instanceof FileNotFoundException
    }

    def "GitLocalException when calls made, repo not init()'d"() {
        given:
        String exceptionMessage = "has not been initialized"
        configuration.create(true).projectName("scratch").projectDirParent(temp)
        gitLocal = createGitLocal(false, false, false)
        gitLocal.prepare(gitPlus)

        when:
        gitLocal.checkoutBranch(new GitBranch("branch"))

        then:
        GitLocalException gle = thrown()
        gle.message.contains(exceptionMessage)

        when:
        gitLocal.commit("dsfd")

        then:
        gle = thrown()
        gle.message.contains(exceptionMessage)

        when:
        gitLocal.createBranch("any")

        then:
        gle = thrown()
        gle.message.contains(exceptionMessage)

        when:
        gitLocal.getOrigin()

        then:
        gle = thrown()
        gle.message.contains(exceptionMessage)

        when:
        gitLocal.push(true, false)

        then:
        gle = thrown()
        gle.message.contains(exceptionMessage)

    }

    def "GitLocalException when calls made, repo does not exist"() {
        given:
        String exceptionMessage = "has not been initialized"
        configuration.create(true).projectName("scratch").projectDirParent(temp)
        gitLocal = createGitLocal(false, false, false)

        when:
        def status = gitLocal.status()

        then:
        status != null
        status.clean

        when:
        gitLocal.branches()

        then:
        GitLocalException gle = thrown()
        gle.message.contains(exceptionMessage)

        when:
        gitLocal.currentBranch()

        then:
        gle = thrown()
        gle.message.contains(exceptionMessage)
    }


    def "commit returns id"() {
        given:
        createScratchRepo()
        createFileAndAddToGit()

        when:
        GitSHA sha = gitLocal.commit('commit 1')

        then: "invalid SHA would throw exception"
        noExceptionThrown()
        sha != null
    }

    def "commits"() {
        given:
        createScratchRepo()
        File f = createAFile()
        gitLocal.add(f)
        gitLocal.commit('commit 1')
        gitLocal.createBranch('develop')
        gitLocal.checkoutBranch(gitLocal.developBranch())
        File f1 = createAFile()
        gitLocal.add(f1)
        gitLocal.commit('commit 2')

        when:
        ImmutableList<GitCommit> developCommits = gitLocal.extractDevelopCommits()
        List<GitCommit> masterCommits = gitLocal.extractMasterCommits()
        def iterator = developCommits.iterator()



        then:
        developCommits.size() == 2
        masterCommits.size() == 1
        GitCommit commit2 = iterator.next()
        GitCommit commit1 = iterator.next()
        commit2.getFullMessage() == ('commit 2')
        commit1.getFullMessage() == ('commit 1')
        commit2.getHash() != null
        commit2.getHash().length() == 40
        developCommits.asList().get(0).hash == gitLocal.headDevelopCommitSHA().sha
        gitLocal.headDevelopCommitSHA().sha == gitLocal.headDevelopCommitSHA().sha
    }


    def "masterBranch"() {
        given:
        when(mockGitProvider.openRepository(any(GitLocalConfiguration))).thenReturn(mockGit)
        gitLocal = createGitLocal(true, true, true)

        expect:
        gitLocal.masterBranch() == new GitBranch("master")
    }

/**
 * Tested in DefaultGitHubRemote2 - easier that way
 */
    def "head commit"() {
        expect: true
    }

    private void createScratchRepo() {
        configuration.create(true).projectName("scratch").projectDirParent(temp)
        gitLocal = createGitLocal(false, false, false)
        gitLocal.createAndInitialise()
    }

    private GitLocal createGitLocal(boolean usingMockGit, boolean usingMockInitChecker, boolean initDone) {

        createGitLocal(usingMockGit, usingMockInitChecker, initDone, false)
    }

    private GitLocal createGitLocal(boolean usingMockGit, boolean usingMockInitChecker, boolean initDone, boolean useMockCloner) {
        GitInitChecker initCheckerUsed = (usingMockInitChecker) ? mockInitChecker : initChecker
        GitProvider gitProvider1 = usingMockGit ? mockGitProvider : gitProvider
        cloner = useMockCloner ? new MockGitCloner() : new DefaultGitCloner()

        if (usingMockInitChecker) {
            mockInitChecker.isInitDone() >> initDone
        }
        gitLocal = new DefaultGitLocal(branchConfigProvider, gitProvider1, configuration, initCheckerUsed, cloner)
        gitLocal.projectName = 'wiggly'
        gitLocal.prepare(gitPlus)
        return gitLocal

    }


    private File createAFile() {
        File f = new File(configuration.projectDir(), "test.md")
        f.createNewFile()
        return f
    }

    private void createFileAndAddToGit() {
        File f = createAFile()
        gitLocal.add(f)
    }


}
