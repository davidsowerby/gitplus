package uk.q3c.build.gitplus.local

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.*
import org.eclipse.jgit.lib.BranchConfig
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.StoredConfig
import org.eclipse.jgit.transport.PushResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.DefaultGitPlus
import uk.q3c.build.gitplus.gitplus.FileDeleteApprover
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.gitplus.GitPlusConfigurationException
import uk.q3c.build.gitplus.remote.DefaultGitRemoteConfiguration
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration

import static uk.q3c.build.gitplus.local.CloneExistsResponse.*

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
    GitPlus gitPlus = Mock(GitPlus)

    def setup() {
        cloner = new MockGitCloner()
        gitProvider = new DefaultGitProvider()
        configuration = new DefaultGitLocalConfiguration()
        mockGitProvider.openRepository(configuration) >> mockGit
        temp = temporaryFolder.getRoot()
        gitPlus.remote >> gitRemote
        gitRemote.repoUser >> 'davidsowerby'
        gitRemote.repoName >> 'scratch'
        gitPlus.taggerName() >> "person"
        gitPlus.taggerEmail() >> "@email"

    }



    def "init prepares directory, invokes projectCreator and adds projectDir to Git"() {
        given:
        configuration.projectDirParent(temp).projectName('dummy').projectCreator(mockProjectCreator)
        gitLocal = createGitLocal(false, false, false)

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

        when:
        gitLocal.createAndInitialise()

        then:
        new File(temp, "scratch/.git").exists()
        gitLocal.initDone
    }

    def "set and get origin, sets to remote"() {
        given:
        String remoteCloneUrl = 'https://remoteUrl'
        gitRemote.cloneUrl() >> remoteCloneUrl
        gitLocal = createGitLocal(false, false, false)
        configuration.create(true).projectDirParent(temp).projectName('dummy')


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
        gitLocal = createGitLocal(false, false, false)
        configuration.create(true).projectDirParent(temp).projectName('dummy')


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
        gitLocal.getOrigin()

        then: "throws exception"
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
        gitLocal.getOrigin()

        then: "throws exception"
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
        initChecker.initDone >> { throw new RuntimeException("fake") }


        when:
        gitLocal = createGitLocal(true, true, false)
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


    def "checkoutNewBranch with remote set and active, sets up tracking branch"() {
        given:
        CheckoutCommand checkout = Mock(CheckoutCommand)
        mockGit.checkout() >> checkout
        mockGit.repository >> repo
        gitRemote.repoUser >> 'davidsowerby'
        gitRemote.repoName >> 'scratch'
        configuration.projectName('scratch')
        gitLocal = createGitLocal(true, true, true)
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


    def "clone"() {
        given:
        gitRemote.repoUser >> 'davidsowerby'
        gitRemote.repoName >> 'scratch'
        gitRemote.cloneUrl() >> 'https://github.com/davidsowerby/scratch.git'
        gitRemote.repoBaselUrl() >> 'https://github.com/davidsowerby/scratch'
        configuration.cloneFromRemote(true).projectDirParent(temp).projectName('scratch')
        gitLocal = createGitLocal(false, false, false)


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


    def "setOrigin failure cause GLE"() {
        given:
        configuration.projectName = 'wiggly'
        gitLocal = createGitLocal(true, true, true)
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

        when:
        gitLocal.setOrigin()

        then:
        GitLocalException gle = thrown()
        gle.message == "Unable to set origin"
    }


    def "currentCommitHash gets exception, throw GLE"() {

        given:
        gitLocal = createGitLocal(true, true, true)
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
        gitPlus = Mock(GitPlus)
        gitRemote = Mock(GitRemote)
        gitRemote.repoName >> notSpecified
        GitRemoteConfiguration remoteConfig = new DefaultGitRemoteConfiguration()
        remoteConfig.repoUser(notSpecified).repoName(notSpecified)
        gitRemote.configuration >> remoteConfig
        gitPlus.remote >> gitRemote

        when: "no project name and no repoName"
        gitLocal = createGitLocal(true, true, true)

        then: "exception thrown"
        thrown GitPlusConfigurationException

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
        gitLocal.prepare(gitPlus)
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
        def gitLocal = new DefaultGitLocal(branchConfigProvider, gitProvider1, configuration, initCheckerUsed, cloner)
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
