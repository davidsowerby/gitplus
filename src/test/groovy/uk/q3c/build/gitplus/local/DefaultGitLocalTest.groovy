package uk.q3c.build.gitplus.local

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.*
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
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.remote.GitRemote

import static uk.q3c.build.gitplus.local.CloneExistsResponse.DELETE
import static uk.q3c.build.gitplus.local.CloneExistsResponse.PULL

/**
 * Created by David Sowerby on 17 Mar 2016
 */
class DefaultGitLocalTest extends Specification {

    static String notSpecified = 'not specified'
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
    GitRemote gitRemote = Mock(GitRemote)
    DefaultGitLocalConfiguration configuration
    Git mockGit = Mock(Git)

    GitProvider gitProvider
    GitProvider mockGitProvider = Mock(GitProvider)
    Repository repo = Mock(Repository)
    GitInitChecker initChecker = new DefaultGitInitChecker()
    GitInitChecker mockInitChecker = Mock(GitInitChecker)
    GitCloner cloner


    BranchConfigProvider branchConfigProvider = Mock(BranchConfigProvider)
    BranchConfig branchConfig = Mock(BranchConfig)
    ProjectCreator mockProjectCreator = Mock(ProjectCreator)
    String baseUrl = "repo/base"
    String wikiUrl = "repo/base/wiki"

    def setup() {
        cloner = new MockGitCloner()
        gitProvider = new DefaultGitProvider()
        configuration = new DefaultGitLocalConfiguration()
        mockGitProvider.openRepository(configuration) >> mockGit
        temp = temporaryFolder.getRoot()

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
        configuration.projectDirParent(temp).projectName('dummy').projectCreator(mockProjectCreator)
        gitLocal = createGitLocal(false, false, false)
        gitLocal.prepare(gitRemote)

        when:
        gitLocal.init()

        then:
        File projectDir = new File(temp, 'dummy')
        new File(projectDir, ".git").exists()
        1 * mockProjectCreator.invoke(configuration)
        gitLocal.status().isClean()
        gitLocal.initDone
    }


    def "create local repo"() {
        given:
        configuration.create(true).projectName("scratch").projectDirParent(temp)
        gitLocal = createGitLocal(false, false, false)
        gitLocal.prepare(gitRemote)

        when:
        gitLocal.createAndInitialise()

        then:
        new File(temp, "scratch/.git").exists()
        gitLocal.initDone
    }

    def "set and get origin, sets to remote"() {
        given:
        String remoteCloneUrl = 'https://remoteUrl'
        GitRemote remote = Mock(GitRemote)
        remote.repoUser >> 'davidsowerby'
        remote.repoName >> 'scratch'
        remote.cloneUrl() >> remoteCloneUrl
        gitLocal = createGitLocal(false, false, false)
        configuration.create(true).projectDirParent(temp).projectName('dummy')
        gitLocal.remote = remote


        when:
        gitLocal.prepare(remote)
        gitLocal.createAndInitialise()
        gitLocal.setOrigin()

        then:
        gitLocal.getOrigin() == remoteCloneUrl
    }

    def "set origin, git throws exception"() {
        given:
        gitRemote.cloneUrl() >> { throw new NullPointerException() }
        mockGit.getRepository() >> { throw new IOException() }
        gitLocal = createGitLocal(false, false, false)
        configuration.create(true).projectDirParent(temp).projectName('dummy')
        gitLocal.prepare(gitRemote)


        when:
        gitLocal.setOrigin()

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Unable to set origin")

    }

    def "getOrigin has no origin defined"() {

        given:
        Repository repository = Mock(Repository)
        mockGit.repository >> repository
        StoredConfig storedConfig = Mock(StoredConfig)
        repository.config >> storedConfig
        gitLocal = createGitLocal(true, true, true)

        when: "there is no origin section in stored config"
        gitLocal.prepare(gitRemote)
        gitLocal.getOrigin()

        then: "throws exception"
        1 * gitRemote.repoName >> 'wiggly'
        1 * storedConfig.getSubsections(DefaultGitPlus.REMOTE) >> ImmutableSet.of("a")
        thrown GitLocalException
    }

    def "retrieving origin throws exception, catch & throw GitLocalException"() {

        given:
        Repository repository = Mock(Repository)
        mockGit.repository >> repository
        StoredConfig storedConfig = Mock(StoredConfig)
        repository.config >> storedConfig
        gitLocal = createGitLocal(true, true, true)

        when: "there is no origin section in stored config"
        gitLocal.prepare(gitRemote)
        gitLocal.getOrigin()

        then: "throws exception"
        1 * gitRemote.repoName >> 'wiggly'
        1 * storedConfig.getSubsections(DefaultGitPlus.REMOTE) >> { throw new NullPointerException() }
        thrown GitLocalException
    }

    def "set origin from remote"() {
        given:
        gitRemote.repoUser >> 'davidsowerby'
        gitRemote.repoName >> 'scratch'
        final String remoteUrl = 'https://github.com/davidsowerby/scratch.git'
        gitRemote.cloneUrl() >> remoteUrl
        gitLocal = createGitLocal(false, false, false)
        configuration.create(true).projectDirParent(temp).projectName('dummy')
        gitLocal.remote = gitRemote
        gitLocal.prepare(gitRemote)

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
        configuration.projectName = 'wiggly'
        gitLocal = createGitLocal(true, false, false)
        initChecker.initDone >> { throw new NullPointerException() }

        when:
        gitLocal.createAndInitialise()

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Unable to create local repo")
    }

    def "create called but not set in config, call is ignored"() {
        given:
        configuration.create = false
        gitLocal = createGitLocal(false, false, false)

        when:
        gitLocal.createAndInitialise()

        then:
        0 * configuration.projectDir()
    }

    def "clone called but not set in config, call is ignored"() {
        given:
        configuration.cloneFromRemote = false
        gitLocal = createGitLocal(false, false, false)

        when:
        gitLocal.cloneRemote()

        then:
        0 * configuration.projectDir()
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
        configuration.projectName = 'wiggly'
        gitLocal = createGitLocal(true, true, true)
        gitLocal.remote = gitRemote
        gitLocal.prepare(gitRemote)

        when:
        gitLocal.push(false, false)

        then:
        1 * mockGit.getRepository() >> repo
        1 * repo.getBranch() >> null
        GitLocalException gle = thrown()
        gle.message.contains("Unable to push")
        gle.cause instanceof GitLocalException
        gle.cause.message.contains("Unable to get current branch")
    }

    def "push with all valid is successful"() {
        given:
        configuration.projectName = 'wiggly'
        gitLocal = createGitLocal(true, true, true)
        gitLocal.remote = gitRemote
        gitLocal.prepare(gitRemote)
        CredentialsProvider credentialsProvider = Mock(CredentialsProvider)
        gitRemote.getCredentialsProvider() >> credentialsProvider
        Repository repo = Mock(Repository)
        PushCommand pc = Mock(PushCommand)

        PushResult pushResult1 = Mock(PushResult)
        PushResult pushResult2 = Mock(PushResult)
        RemoteRefUpdate update1 = Mock(RemoteRefUpdate)
        RemoteRefUpdate update2 = Mock(RemoteRefUpdate)
        RemoteRefUpdate update3 = Mock(RemoteRefUpdate)
        RemoteRefUpdate update4 = Mock(RemoteRefUpdate)
        List<RemoteRefUpdate> updates1 = ImmutableList.of(update1)
        List<RemoteRefUpdate> updates2 = ImmutableList.of(update2, update3, update4)
        Iterable<PushResult> results = ImmutableList.of(pushResult1, pushResult2)
        pushResult1.getRemoteUpdates() >> updates1
        pushResult2.getRemoteUpdates() >> updates2

        when:
        PushResponse response = gitLocal.push(true, true)

        then:
        mockGit.getRepository() >> repo
        repo.isBare() >> false
        repo.getBranch() >> 'develop'

//        then:
        1 * gitRemote.active >> false
        mockGit.push() >> pc
        1 * pc.add('develop') >> pc
        1 * pc.setCredentialsProvider(credentialsProvider) >> pc
        1 * pc.setPushTags()
        1 * pc.setForce(true)
        1 * pc.call() >> results
        update1.status >> RemoteRefUpdate.Status.OK

        response.getUpdates().size() == 4
        response.getUpdates().containsAll(ImmutableList.of(update1, update2, update3, update4))

    }


    def "push with result failure throws PushException wrapped in GLE"() {
        given:
        configuration.projectName = 'wiggly'
        gitLocal = createGitLocal(true, true, true)
        gitLocal.remote = gitRemote
        CredentialsProvider credentialsProvider = Mock(CredentialsProvider)
        gitRemote.getCredentialsProvider() >> credentialsProvider
        gitLocal.prepare(gitRemote)
        Repository repo = Mock(Repository)
        PushCommand pc = Mock(PushCommand)

        PushResult pushResult1 = Mock(PushResult)
        PushResult pushResult2 = Mock(PushResult)
        RemoteRefUpdate update1 = Mock(RemoteRefUpdate)
        RemoteRefUpdate update2 = Mock(RemoteRefUpdate)
        RemoteRefUpdate update3 = Mock(RemoteRefUpdate)
        RemoteRefUpdate update4 = Mock(RemoteRefUpdate)
        List<RemoteRefUpdate> updates1 = ImmutableList.of(update1)
        List<RemoteRefUpdate> updates2 = ImmutableList.of(update2, update3, update4)
        Iterable<PushResult> results = ImmutableList.of(pushResult1, pushResult2)
        pushResult1.getRemoteUpdates() >> updates1
        pushResult2.getRemoteUpdates() >> updates2

        when:
        PushResponse response = gitLocal.push(false, false)

        then:
        mockGit.getRepository() >> repo
        repo.isBare() >> false
        repo.getBranch() >> 'develop'

//        then:
        1 * gitRemote.active >> false
        mockGit.push() >> pc
        1 * pc.add('develop') >> pc
        1 * pc.setCredentialsProvider(credentialsProvider) >> pc
        0 * pc.setPushTags()
        1 * pc.setForce(false)
        1 * pc.call() >> results
        update1.status >> RemoteRefUpdate.Status.REJECTED_NODELETE

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
        Repository repository = Mock(Repository)
        mockGit.repository >> repository
        Ref simplyCdRef = Mock(Ref)
        repository.findRef("simplycd") >> simplyCdRef
        MergeCommand mergeCommand = Mock(MergeCommand)
        mockGit.merge() >> mergeCommand
        MergeResult mergeResult = Mock(MergeResult)
        mergeCommand.call() >> mergeResult
        MergeResult.MergeStatus mergeStatus = Mock(MergeResult.MergeStatus)
        mergeResult.getMergeStatus() >> mergeStatus
        mergeResult.toString() >> "totally banjaxed"
        mergeStatus.isSuccessful() >> false
        gitLocal = createGitLocal(true, true, true)
        gitLocal.localConfiguration.projectName("wiggly")
        gitLocal.prepare(gitRemote)
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
        Repository repository = Mock(Repository)
        mockGit.repository >>> { throw new IllegalArgumentException() }
        gitLocal = createGitLocal(true, true, true)
        gitLocal.localConfiguration.projectName("wiggly")
        gitLocal.prepare(gitRemote)
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
        final String testProject = 'q3c-testUtil'
        gitRemote.repoUser >> 'davidsowerby'
        gitRemote.repoName >> testProject
        gitRemote.repoBaselUrl() >> 'https://github.com/davidsowerby/' + testProject
        gitRemote.cloneUrl() >> 'https://github.com/davidsowerby/' + testProject + ".git"
        gitLocal = createGitLocal(false, false, false)
        gitLocal.remote = gitRemote
        configuration.projectName(testProject).projectDirParent(temp).cloneFromRemote(true)
        String commitHash = 'f6d1f2269daefeb4375ef5d2a2c2400101df6aa5'
        gitLocal.prepare(gitRemote)

        when:
        gitLocal.cloneRemote()

        then:
        gitLocal.currentBranch().equals(gitLocal.developBranch())

        when:
        gitLocal.checkoutCommit(new GitSHA(commitHash))

        then:
        gitLocal.currentCommitHash().equals(commitHash)


        when:
        gitLocal.checkoutNewBranch(new GitBranch('wiggly'))

        then:
        gitLocal.currentBranch().equals(new GitBranch('wiggly'))
        gitLocal.currentCommitHash().equals(commitHash)
    }

    /**
     * Uses existing project as older commits will not then be subject to change
     */
    def "checkout specific commit using new branch"() {

        given:
        final String testProject = 'q3c-testUtil'
        gitRemote.repoUser >> 'davidsowerby'
        gitRemote.repoName >> testProject
        gitRemote.repoBaselUrl() >> 'https://github.com/davidsowerby/' + testProject
        gitRemote.cloneUrl() >> 'https://github.com/davidsowerby/' + testProject + ".git"
        gitLocal = createGitLocal(false, false, false)
        gitLocal.remote = gitRemote
        configuration.projectName(testProject).projectDirParent(temp).cloneFromRemote(true)
        String commitHash = 'f6d1f2269daefeb4375ef5d2a2c2400101df6aa5'
        gitLocal.prepare(gitRemote)

        when:
        gitLocal.cloneRemote()

        then:
        gitLocal.currentBranch().equals(gitLocal.developBranch())

        when:
        gitLocal.checkoutCommit(new GitSHA(commitHash), "temptest")

        then:
        gitLocal.currentCommitHash().equals(commitHash)
        gitLocal.currentBranch().name == "temptest"
    }


    def "checkoutNewBranch with remote set and active, sets up tracking branch"() {
        given:
        CheckoutCommand checkout = Mock(CheckoutCommand)
        mockGit.checkout() >> checkout
        mockGit.repository >> repo
        gitRemote.repoUser >> 'davidsowerby'
        gitRemote.repoName >> 'scratch'
        configuration.projectName('scratch')
        gitLocal = createGitLocal(true, true, true)
        gitLocal.prepare(gitRemote)
        StoredConfig storedConfig = Mock(StoredConfig)
        PushCommand pc = Mock(PushCommand)
        Iterable<PushResult> results = ImmutableList.of()


        when:
        gitLocal.checkoutNewBranch(new GitBranch('wiggly'))

        then:
        gitRemote.active >> true
        repo.isBare() >> false
        repo.getConfig() >> storedConfig
        repo.getBranch() >> 'wiggly'
        branchConfig.trackingBranch >> null
        branchConfigProvider.get(storedConfig, 'wiggly') >> branchConfig
        1 * checkout.setCreateBranch(true) >> checkout
        checkout.setName('wiggly') >> checkout
        2 * checkout.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM) >> checkout
        2 * checkout.setStartPoint("wiggly") >> checkout
        3 * checkout.call()
        1 * mockGit.push() >> pc

        1 * pc.add('wiggly') >> pc
        1 * pc.setCredentialsProvider(_) >> pc
        1 * pc.call() >> results
    }

    def "checkoutNewBranch or checkoutCommit failure causes GLE"() {
        given:
        CheckoutCommand checkout = Mock(CheckoutCommand)
        mockGit.checkout() >> checkout
        mockGit.repository >> repo
        checkout.call() >> { throw new IOException() }
        gitRemote.active >> false
        configuration.projectName = 'wiggly'
        gitLocal = createGitLocal(true, true, true)
        gitLocal.prepare(gitRemote)


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
        gitLocal.prepare(gitRemote)

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
        gitLocal.prepare(gitRemote)

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


    def "tags"() {
        given:
        createScratchRepo()
        File f1 = createAFile()
        gitLocal.add(f1)
        gitLocal.commit('commit 1')
        gitLocal.tag("0.1", 'body')
        File f2 = createAFile()
        gitLocal.add(f2)
        gitLocal.commit('commit 2')
        gitLocal.tag("0.2", "body")

        when:
        List<Tag> tags = gitLocal.tags()
        Tag tag1 = tags.get(0)
        Tag tag2 = tags.get(1)

        then:
        tag1.getTagName() == ("0.1")
        tag1.getFullMessage() == ("body")
        tag2.getTagName() == ("0.2")
        tag2.getFullMessage() == ("body")

    }

    def "tag throws exception"() {
        given:
        TagCommand tc = Mock(TagCommand)
        mockGit.tag() >> tc
        tc.call() >> { throw new NullPointerException() }
        configuration.create(true).projectName("scratch").projectDirParent(temp)
        gitLocal = createGitLocal(true, true, true)
        gitLocal.prepare(gitRemote)

        when:
        gitLocal.tag('x', 'body')

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Unable to tag")

        when:
        gitLocal.tagLightweight('x')

        then:
        gle = thrown()
        gle.message.contains("Unable to tag")
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


    def "clone"() {
        given:
        gitRemote.repoUser >> 'davidsowerby'
        gitRemote.repoName >> 'scratch'
        gitRemote.cloneUrl() >> 'https://github.com/davidsowerby/scratch.git'
        gitRemote.repoBaselUrl() >> 'https://github.com/davidsowerby/scratch'
        configuration.cloneFromRemote(true).projectDirParent(temp).projectName('scratch')
        gitLocal = createGitLocal(false, false, false)
        gitLocal.remote = gitRemote
        gitLocal.prepare(gitRemote)


        when:
        gitLocal.cloneRemote()

        then:
        gitLocal.extractMasterCommits().size() > 0

        when:
        gitLocal.cloneRemote() // second time, so local repo already exists

        then:
        GitLocalException gle = thrown()
        gle.cause.message == "Git clone called, when Git local directory already exists"

        when:
        gitLocal.cloneExistsResponse(DELETE) // but has no approver
        gitLocal.cloneRemote()

        then:
        gle = thrown()
        gle.cause.message.contains("Delete of directory not approved")

        when: "actively denied"
        gitLocal.cloneExistsResponse(DELETE).fileDeleteApprover(new TestDirDeleteDenier())
        gitLocal.cloneRemote()

        then:
        gle = thrown()
        gle.cause.message.contains("Delete of directory not approved")

        when:
        gitLocal.cloneExistsResponse(DELETE).fileDeleteApprover(new TestDirDeleteApprover(temp))
        gitLocal.cloneRemote()

        then:
        gitLocal.extractMasterCommits().size() > 0
    }

    def "clone with response set to PULL"() {
        given:
        gitRemote.repoUser >> 'davidsowerby'
        gitRemote.repoName >> 'scratch'
        gitRemote.cloneUrl() >> 'https://github.com/davidsowerby/scratch.git'
        configuration.cloneFromRemote(true).projectDirParent(temp).projectName('scratch').cloneExistsResponse(PULL)
        gitLocal = createGitLocal(true, true, true)
        gitLocal.remote = gitRemote
        gitLocal.prepare(gitRemote)
//        gitLocal.prepare() // don't do this, it replaces mockGit with a real one
        FileUtils.forceMkdir(new File(temp, 'scratch'))
        PullCommand pc = Mock(PullCommand)

        when:
        gitLocal.cloneRemote()

        then:
        1 * mockGit.pull() >> pc
    }

    def "status works or fails"() {
        given:
        StatusCommand sc = Mock(StatusCommand)
        Status status = Mock(Status)
        configuration.projectName = 'wiggly'
        gitLocal = createGitLocal(true, true, true)
        gitLocal.remote = gitRemote
        gitLocal.prepare(gitRemote)
        IOException ioe = new IOException()


        when:
        gitLocal.status()

        then:
        1 * mockGit.status() >> sc
        1 * sc.call() >> status

        when: "call fails"
        gitLocal.status()

        then:
        1 * mockGit.status() >> { throw ioe }
        GitLocalException gle = thrown()
        gle.cause == ioe

    }

    def "pull failure causes GLE"() {
        given:
        PullCommand pull = Mock(PullCommand)
        configuration.projectName = 'wiggly'
        gitLocal = createGitLocal(true, true, true)
        gitLocal.remote = gitRemote
        gitLocal.prepare(gitRemote)
        mockGit.pull() >> pull
        pull.call() >> { throw new IOException() }

        when:
        gitLocal.pull()

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Pull of current branch failed")
    }

    def "pull with branch name failure causes GLE"() {
        given:
        String branch = 'master'
        PullCommand pull = Mock(PullCommand)
        configuration.projectName = 'wiggly'
        gitLocal = createGitLocal(true, true, true)
        gitLocal.remote = gitRemote
        gitLocal.prepare(gitRemote)
        mockGit.pull() >> pull


        when:
        gitLocal.pull(branch)

        then:
        1 * pull.setRemoteBranchName(branch)

        then:
        1 * pull.call() >> { throw new IOException() }
        GitLocalException gle = thrown()
        gle.message.contains("Pull of 'master' branch failed")
    }

    def "masterBranch"() {
        given:
        gitLocal = createGitLocal(true, true, true)

        expect:
        gitLocal.masterBranch() == new GitBranch("master")
    }

    def "setOrigin failure cause GLE"() {
        given:
        configuration.projectName = 'wiggly'
        gitLocal = createGitLocal(true, true, true)
        gitLocal.remote = gitRemote
        gitLocal.prepare(gitRemote)
        mockGit.repository >> repo
        repo.config >> { throw new IOException() }

        when:
        gitLocal.setOrigin('scratch')

        then:
        GitLocalException gle = thrown()
        gle.message.contains("Unable to set origin to scratch")

    }

    def "getTags() fails"() {
        given:
        gitRemote.repoUser >> 'davidsowerby'
        gitRemote.repoName >> 'scratch'
        configuration.cloneFromRemote(true).projectDirParent(temp).projectName('scratch')
        gitLocal = createGitLocal(true, true, true)

        when:
        gitLocal.tags()

        then:
        mockGit.tagList() >> { throw new IOException() }
        GitLocalException gle = thrown()
        gle.message.contains("Unable to read Git status")
    }

    def "tags, lightweight and annotated"() {
        given:
        createScratchRepo()
        createFileAndAddToGit()
        gitLocal.commit('commit 1')

        when:
        gitLocal.tag('fat', 'body')
        gitLocal.tagLightweight('thin')
        List<Tag> tags = gitLocal.tags()

        then:
        tags.size() == 2
        tags.get(0).getTagType() == Tag.TagType.ANNOTATED
        tags.get(1).getTagType() == Tag.TagType.LIGHTWEIGHT
        tags.get(0).getFullMessage() == 'body'
        tags.get(1).getFullMessage() == ""
        tags.get(0).getReleaseDate() != null
        tags.get(1).getReleaseDate() != null
    }


    def "extract commits fails"() {
        given:
        gitRemote.repoUser >> 'davidsowerby'
        gitRemote.repoName >> 'scratch'
        configuration.projectDirParent(temp)
        mockGit.log() >> { throw new IOException() }
        gitLocal = createGitLocal(false, false, false)

        when:
        gitLocal.extractDevelopCommits()

        then:
        GitLocalException gle = thrown()
        gle.message == "Reading commits for branch 'develop' failed"
    }

    def "setOrigin fails, throws GitLocalException"() {

        given:
        StoredConfig config = Mock(StoredConfig)
        config.save() >> { throw new NullPointerException() }
        gitLocal = createGitLocal(false, false, false)
        gitLocal.remote = gitRemote

        when:
        gitLocal.setOrigin()

        then:
        GitLocalException gle = thrown()
        gle.message == "Unable to set origin"
    }


    def "currentCommitHash gets exception, throw GLE"() {

        given:
        gitLocal = createGitLocal(true, true, true)
        gitLocal.remote = gitRemote
        Repository repo = Mock(Repository)
        mockGit.getRepository() >> repo

        when:
        gitLocal.currentCommitHash()

        then:
        repo.getRef("HEAD") >> { throw new IOException() }
        GitLocalException gle = thrown()
        gle.message == "Unable to retrieve current revision"
    }


    def "prepare takes projectName from remote if not set"() {
        given:
        gitLocal = createGitLocal(true, true, true)
        GitRemote gitRemote2 = Mock(GitRemote)
        gitRemote.repoName >> notSpecified
        gitRemote2.repoName >> 'biscuit'

        when: // no project name and no repoName
        gitLocal.prepare(gitRemote)

        then:
        thrown GitPlusConfigurationException

        when:
        gitRemote.repoName('wiggly')
        gitLocal.prepare(gitRemote2)

        then:

        noExceptionThrown()
        gitLocal.projectName == 'biscuit'

    }

    def "clone uses base url "() {
        given:
        gitRemote.repoBaselUrl() >> baseUrl
        gitRemote.wikiUrl() >> wikiUrl
        configuration.cloneFromRemote = true
        configuration.projectName = 'wiggly'
        configuration.projectDirParent = temp
        gitRemote.repoBaselUrl() >> baseUrl
        gitLocal = createGitLocal(true, true, true, true)
        gitLocal.remote = gitRemote
        MockGitCloner mockCloner = cloner as MockGitCloner

        when:
        gitLocal.cloneRemote()

        then:

        mockCloner.cloned
        mockCloner.localDir == new File(temp, 'wiggly')
        mockCloner.remoteUrl == baseUrl

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
        gitLocal.prepare(gitRemote)
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
        new DefaultGitLocal(branchConfigProvider, gitProvider1, configuration, initCheckerUsed, cloner)
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
