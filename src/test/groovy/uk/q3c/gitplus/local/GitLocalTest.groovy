package uk.q3c.gitplus.local

import com.google.common.collect.ImmutableSet
import org.eclipse.jgit.api.Git
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.remote.GitRemote

/**
 * Created by David Sowerby on 17 Mar 2016
 */
class GitLocalTest extends Specification {

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
        configuration.validate()
        gitLocal = new GitLocal(configuration)

        when:
        gitLocal.createLocalRepo()

        then:
        new File(temp, "scratch/.git").exists()
    }

    def "create local repo failure throws GitLocalException"() {
        given:
        configuration.projectName("scratch").projectDirParent(temp).createLocalRepo(true)
//        configuration.validate() not validating makes projectDir null to induce failure
        gitLocal = new GitLocal(configuration)

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

    def "GitLocalException when init call fails"() {
        given:
        gitLocal = new GitLocal(configuration)

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

    def "GitLocalException   when calls made, repo does not exist"() {
        given:
        configuration.createLocalRepo(true).projectName("scratch").projectDirParent(temp).createLocalRepo(true)
        gitLocal = new GitLocal(configuration)

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
        ImmutableSet<GitCommit> developCommits = gitLocal.extractDevelopCommits()
        Set<GitCommit> masterCommits = gitLocal.extractMasterCommits()
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
        configuration.cloneRemoteRepo(true).remoteRepoFullName('davidsowerby/scratch').projectDirParent(temp).apiToken('x')
        configuration.validate()
        gitLocal = new GitLocal(configuration)

        when:
        gitLocal.cloneRemote()

        then:
        gitLocal.extractMasterCommits().size() > 0
    }

    def "getTags() fails"() {
        given:
        configuration.cloneRemoteRepo(true).remoteRepoFullName('davidsowerby/scratch').projectDirParent(temp).apiToken('x')
        configuration.validate()
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
