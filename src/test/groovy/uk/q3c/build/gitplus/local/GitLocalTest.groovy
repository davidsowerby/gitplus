package uk.q3c.build.gitplus.local

import com.google.common.collect.ImmutableList
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullCommand
import org.eclipse.jgit.api.PushCommand
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.FileDeleteApprover
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.build.gitplus.remote.GitRemote

import static uk.q3c.build.gitplus.gitplus.GitPlusConfiguration.CloneExistsResponse.DELETE
import static uk.q3c.build.gitplus.gitplus.GitPlusConfiguration.CloneExistsResponse.PULL

/**
 * Created by David Sowerby on 17 Mar 2016
 */
class GitLocalTest extends Specification {

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
    GitPlusConfiguration configuration
    Git mockGit = Mock(Git)

    def setup() {
        configuration = new GitPlusConfiguration()
        temp = temporaryFolder.getRoot()
    }

    def "construct with null configuration throws NPE"() {
        when:
        gitLocal = new GitLocal(null)

        then:
        thrown(NullPointerException)
    }

    def "init prepares directory"() {
        given:
        configuration.projectDir(temp)
        gitLocal = new GitLocal(configuration)

        when:
        gitLocal.init()

        then:
        new File(temp, ".git").exists()
    }


    def "create local repo"() {
        given:
        configuration.createLocalRepo(true).projectName("scratch").projectDirParent(temp).createLocalRepo(true)
        gitLocal = new GitLocal(configuration)

        when:
        gitLocal.createLocalRepo()

        then:
        new File(temp, "scratch/.git").exists()
    }

    def "set origin"() {
        given:
        configuration.createLocalRepo(true).remoteRepoFullName("davidsowerby/scratch").projectDirParent(temp).createLocalRepo(true)
        gitLocal = new GitLocal(configuration)
        gitLocal.createLocalRepo()

        when:
        gitLocal.setOrigin()

        then:
        gitLocal.getOrigin().equals('https://github.com/davidsowerby/scratch.git')
    }

    def "set origin, git throws exception"() {
        given:
        configuration.createLocalRepo(true).remoteRepoFullName("davidsowerby/scratch").projectDirParent(temp).createLocalRepo(true)
        mockGit.getRepository() >> { throw new IOException() }
        gitLocal = new GitLocal(mockGit, configuration)
        gitRemote.getCloneUrl() >> { throw new NullPointerException() }

        when:
        gitLocal.setOrigin()

        then:
        thrown GitLocalException

        when:
        gitLocal.setOrigin(gitRemote)

        then:
        thrown GitLocalException
    }


    def "set origin from remote"() {
        given:
        final String remoteUrl = 'https://github.com/davidsowerby/scratch.git'
        configuration.createLocalRepo(true).remoteRepoFullName("davidsowerby/scratch").projectDirParent(temp).createLocalRepo(true)
        gitRemote.getCloneUrl() >> remoteUrl
        gitLocal = new GitLocal(configuration)
        gitLocal.createLocalRepo()

        when:
        gitLocal.setOrigin(gitRemote)

        then:
        gitLocal.getOrigin().equals(remoteUrl)
    }

    def "create local repo failure throws GitLocalException"() {
        given:
        configuration.projectName("scratch").projectDirParent(temp).createLocalRepo(true)
        gitLocal = new GitLocal(configuration)
        gitLocal.getConfiguration().projectDir(null).projectDirParent(null).projectName(null) // null to induce failure

        when:
        gitLocal.createLocalRepo()

        then:
        thrown GitLocalException
    }

    def "close() .. then call getGit() returns different git instance"() {
        given:
        createScratchRepo()
        Git firstRef = gitLocal.getGit()

        when:
        gitLocal.close()

        then:
        gitLocal.getGit() != firstRef

    }

    def "call close()twice does not cause NPE"() {
        given:
        createScratchRepo()
        Git firstRef = gitLocal.getGit()

        when:
        gitLocal.close()
        gitLocal.close()

        then:
        noExceptionThrown()

    }

    def "push"() {
        given:
        PushCommand pc = Mock(PushCommand)
        CredentialsProvider credentialsProvider = Mock(CredentialsProvider)
        gitRemote.getCredentialsProvider() >> credentialsProvider
        gitLocal = new GitLocal(mockGit, configuration)
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
        PushResponse response = gitLocal.push(gitRemote, true)

        then:
        mockGit.push() >> pc
        pc.setCredentialsProvider(gitRemote.getCredentialsProvider()) >> pc
        1 * pc.setPushTags()
        1 * pc.call() >> results

        response.getUpdates().size() == 4
        response.getUpdates().containsAll(ImmutableList.of(update1, update2, update3, update4))


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
        gitLocal.checkout("develop")

        then:
        gitLocal.currentBranch().equals("develop")

        when:
        gitLocal.checkout("branch")

        then:
        thrown GitLocalException
    }
    /**
     * Uses existing project as older commits will not then be subject to change
     */
    def "checkout specific commit"() {
        given:
        GitPlus gitPlus = new GitPlus()
        configuration = gitPlus.getConfiguration()
        configuration.remoteRepoUser('davidsowerby').remoteRepoName('q3c-testUtil').cloneRemoteRepo(true).projectDirParent(temp)
        gitLocal = gitPlus.getGitLocal()
        String commitHash = 'f6d1f2269daefeb4375ef5d2a2c2400101df6aa5'

        when:
        gitPlus.createOrVerifyRepos()

        then:
        gitLocal.currentBranch().equals('develop')

        when:
        gitLocal.checkout(commitHash)

        then:
        gitLocal.currentRevision().equals(commitHash)
        gitLocal.currentBranch().equals(commitHash)

        when:
        gitLocal.checkout('wiggly', commitHash)

        then:
        gitLocal.currentBranch().equals('wiggly')
        gitLocal.currentRevision().equals(commitHash)

    }

    def "expand ref"() {
        given:
        createScratchRepo()

        expect:
        gitLocal.expandBranchName('master').equals('refs/head/master')

        when:
        gitLocal.expandBranchName(null)

        then:
        thrown NullPointerException
    }

    def "GitLocalException when init call fails"() {
        given:
        gitLocal = new GitLocal(configuration)
        gitLocal.getConfiguration().projectDir(null).projectName(null) // force failure


        when:
        gitLocal.init()

        then:
        thrown GitLocalException

    }

    def "GitLocalException when adding a non-existent file"() {
        given:
        configuration.createLocalRepo(true).projectName("scratch").projectDirParent(temp).createLocalRepo(true).projectDir(temp)
        gitLocal = new GitLocal(configuration)

        when:

        gitLocal.add(new File(temp, "test.md"))

        then:
        thrown GitLocalException
    }

    def "GitLocalException when calls made, repo not init()'d"() {
        given:
        configuration.createLocalRepo(true).projectName("scratch").projectDirParent(temp).createLocalRepo(true).projectDir(temp)
        gitLocal = new GitLocal(configuration)

        when:
        gitLocal.checkout("branch")

        then:
        thrown GitLocalException

        when:
        gitLocal.commit("dsfd")

        then:
        thrown GitLocalException

        when:
        gitLocal.createBranch("any")

        then:
        thrown GitLocalException

        when:
        gitLocal.getOrigin()

        then:
        thrown GitLocalException

        when:
        gitLocal.push(gitRemote, true)

        then:
        thrown GitLocalException

    }

    def "GitLocalException when calls made, repo does not exist"() {
        given:
        configuration.createLocalRepo(true).projectName("scratch").projectDirParent(temp).createLocalRepo(true)
        gitLocal = new GitLocal(configuration)
        gitLocal.getConfiguration().projectDir(null).projectName(null) //  null to induce failure

        when:
        gitLocal.status()

        then:
        thrown GitLocalException

        when:
        gitLocal.branches()

        then:
        thrown GitLocalException

        when:
        gitLocal.currentBranch()

        then:
        thrown GitLocalException
    }

    def "null configuration causes NPE in constructor"() {
        when:
        new GitLocal(null)

        then:
        thrown NullPointerException
    }

    def "tags"() {
        given:
        createScratchRepo()
        File f1 = createAFile()
        gitLocal.add(f1)
        gitLocal.commit('commit 1')
        gitLocal.tag("0.1")
        File f2 = createAFile()
        gitLocal.add(f2)
        gitLocal.commit('commit 2')
        gitLocal.tag("0.2")

        when:
        List<Tag> tags = gitLocal.getTags()
        Tag tag1 = tags.get(0)
        Tag tag2 = tags.get(1)

        then:
        tag1.getTagName().equals("0.1")
        tag1.getFullMessage().equals("Released at version 0.1")
        tag2.getTagName().equals("0.2")
        tag2.getFullMessage().equals("Released at version 0.2")

    }

    def "commits"() {
        given:
        createScratchRepo()
        File f = createAFile()
        gitLocal.add(f)
        gitLocal.commit('commit 1')
        gitLocal.createBranch('develop')
        gitLocal.checkout('develop')
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
        commit2.getFullMessage().equals('commit 2')
        commit1.getFullMessage().equals('commit 1')
        commit2.getHash() != null
        commit2.getHash().length() == 40
    }


    def "clone"() {
        given:
        configuration.cloneRemoteRepo(true).remoteRepoFullName('davidsowerby/scratch').projectDirParent(temp)
        gitLocal = new GitLocal(configuration)


        when:
        gitLocal.cloneRemote()

        then:
        gitLocal.extractMasterCommits().size() > 0

        when:
        gitLocal.cloneRemote() // second time, so local repo already exists

        then:
        thrown GitLocalException

        when:
        gitLocal.getConfiguration().cloneExistsResponse(DELETE) // but has no approver
        gitLocal.cloneRemote()

        then:
        thrown GitLocalException

        when:
        gitLocal.getConfiguration().cloneExistsResponse(DELETE).fileDeleteApprover(new TestDirDeleteDenier())
        gitLocal.cloneRemote()

        then:
        thrown GitLocalException // not approved

        when:
        gitLocal.getConfiguration().cloneExistsResponse(DELETE).fileDeleteApprover(new TestDirDeleteApprover(temp))
        gitLocal.cloneRemote()

        then:
        gitLocal.extractMasterCommits().size() > 0
    }

    def "clone with response set to PULL"() {
        given:
        configuration.cloneRemoteRepo(true).remoteRepoFullName('davidsowerby/scratch').projectDirParent(temp).cloneExistsResponse(PULL)
        gitLocal = new GitLocal(mockGit, configuration)
        FileUtils.forceMkdir(configuration.getProjectDir())  // simulate previous clone
        PullCommand pc = Mock(PullCommand)

        when:
        gitLocal.cloneRemote()

        then:
        1 * mockGit.pull() >> pc


    }

    def "getTags() fails"() {
        given:
        configuration.cloneRemoteRepo(true).remoteRepoFullName('davidsowerby/scratch').projectDirParent(temp)
        gitLocal = new GitLocal(mockGit, configuration)

        when:
        gitLocal.getTags()

        then:
        mockGit.tagList() >> { throw new IOException() }
        thrown GitLocalException
    }

    def "tags, lightweight and annotated"() {
        given:
        createScratchRepo();
        createFileAndAddToGit();
        gitLocal.commit('commit 1')

        when:
        gitLocal.tag('fat')
        gitLocal.tagLightweight('thin')
        List<Tag> tags = gitLocal.getTags()

        then:
        tags.size() == 2
        tags.get(0).getTagType() == Tag.TagType.ANNOTATED
        tags.get(1).getTagType() == Tag.TagType.LIGHTWEIGHT
        tags.get(0).getFullMessage().equals('Released at version fat')
        tags.get(1).getFullMessage().equals("No tag message available")
        tags.get(0).getReleaseDate() != null
        tags.get(1).getReleaseDate() != null

    }

    def "projectDir supplied by configuration"() {
        given:
        File f = new File('.')
        configuration.projectDir(f)
        gitLocal = new GitLocal(configuration)

        expect:
        gitLocal.getProjectDir() == f
    }

    def "configure for wiki"() {
        given:
        configuration.remoteRepoFullName('davidsowerby/scratch').projectDirParent(temp)
        gitLocal = new GitLocal(configuration)

        when:
        gitLocal.configureForWiki()

        then:
        gitLocal.getConfiguration().getRemoteRepoHtmlUrl().equals('https://github.com/davidsowerby/scratch/wiki')
        gitLocal.getConfiguration().getProjectDir().equals(new File(temp, 'scratch.wiki'))
    }

    def "extract commits fails"() {
        given:
        configuration.remoteRepoFullName('davidsowerby/scratch').projectDirParent(temp)
        mockGit.log() >> { throw new IOException() }
        gitLocal = new GitLocal(mockGit, configuration)

        when:
        gitLocal.extractDevelopCommits()

        then:
        thrown GitLocalException
    }


    private void createScratchRepo() {
        configuration.createLocalRepo(true).projectName("scratch").projectDirParent(temp).createLocalRepo(true).projectDir(temp)
        gitLocal = new GitLocal(configuration)
        gitLocal.createLocalRepo()
    }

    private File createAFile() {
        File f = new File(configuration.getProjectDir(), "test.md")
        f.createNewFile()
        return f;
    }

    private void createFileAndAddToGit() {
        File f = createAFile();
        gitLocal.add(f)
    }


}
