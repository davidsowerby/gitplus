package uk.q3c.gitplus.git;

import com.google.common.collect.ImmutableSet;
import com.mycila.testing.junit.MycilaJunitRunner;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MycilaJunitRunner.class)
public class GitHandlerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    RebaseFixupLastCommit rebaseFixupLastCommit;
    GitHandler handler;
    private Git git;
    private File gitDir;
    private File projectDir;
    private Repository repo;

    @Before
    public void setup() throws IOException {
        createRepository();
        handler = new GitHandler();
        handler.setProjectDir(projectDir);
    }

    public void createRepository() throws IOException {
        gitDir = new File(temporaryFolder.getRoot(), "git");
        projectDir = new File(gitDir, "scratch");
        FileUtils.forceMkdir(projectDir);
        File gitFile = new File(projectDir, ".git");
        repo = new FileRepository(gitFile);
        repo.create();
        git = new Git(repo);

    }

    @After
    public void tearDown() {
        if (repo != null) {
            repo.close();
        }
    }

    @Test
    public void developIsAheadOfMaster() throws IOException, GitAPIException {
        //given
        File f1 = createAndAddFile(1);
        git.commit()
           .setMessage("commit1")
           .call();
        //when
        handler.createBranch("develop");
        handler.checkout("develop");
        //then
        assertThat(handler.developIsAheadOfMaster(projectDir)).isFalse();
        //given
        File f2 = createAndAddFile(2);
        //        when
        git.commit()
           .setMessage("commit2")
           .call();
        //then
        assertThat(handler.developIsAheadOfMaster(projectDir)).isTrue();
    }

    private File createAndAddFile(int index) throws IOException, GitAPIException {
        File file = new File(projectDir, "tempFile" + index);
        file.createNewFile();
        addFileToGit(file);
        return file;
    }

    private void addFileToGit(File file) throws IOException, GitAPIException {
        git.add()
           .addFilepattern(file.getName())
           .call();
    }

    @Test
    public void fixupLastCommit() throws IOException, GitAPIException {
        //given
        File f1 = createAndAddFile(1);
        //when
        git.commit()
           .setMessage("commit1")
           .call();
        //then confirm it is committed
        assertThat(handler.hasUncommittedChanges(projectDir)).isFalse();
        //given
        File f2 = createAndAddFile(2);
        git.commit()
           .setMessage("commit2")
           .call();
        File f3 = createAndAddFile(3);
        git.commit()
           .setMessage("commit3")
           .call();
        //then confirm it is committed
        assertThat(handler.hasUncommittedChanges(projectDir)).isFalse();
        assertThat(commitCount()).isEqualTo(3);
        //when squashed
        handler.fixupLastCommit(projectDir);
        //        then
        assertThat(commitCount()).isEqualTo(2);
        assertThat(handler.headCommit("master")
                          .getFullMessage()).isEqualTo("commit2");
    }

    private int commitCount() throws IOException, GitAPIException {
        Iterable<RevCommit> commits = git.log()
                                         .all()
                                         .call();
        int c = 0;
        for (RevCommit commit : commits) {
            c++;
        }
        return c;
    }

    @Test
    public void branchAndMerge() throws IOException, GitAPIException {
        //given

        //when
        File f1 = createAndAddFile(1);
        git.commit()
           .setMessage("commit1")
           .call();

        //then
        assertThat(handler.currentBranch(projectDir)).isEqualTo("master");

        //when
        handler.createBranch("develop");
        handler.checkout("develop");

        //then
        assertThat(handler.currentBranch(projectDir)).isEqualTo("develop");

        File f2 = createAndAddFile(2);
        git.commit()
           .setMessage("commit2")
           .call();

        //when

        //then


        //when
        handler.checkout("master");

        //then
        assertThat(handler.currentBranch(projectDir)).isEqualTo("master");

        //when
        handler.mergeBranch_FastForwardOnly("develop");
        //then
        RevCommit developHead = handler.headCommit("develop");
        RevCommit masterHead = handler.headCommit("master");

        assertThat(developHead.equals(masterHead)).isTrue();
    }


    @Test
    public void clone_push() throws IOException, GitAPIException {
        //when
        FileUtils.forceDelete(new File(temporaryFolder.getRoot(), "git/scratch"));
        handler.cloneRepo(gitDir, "master");
        //        then
        handler.commit("commit1");
        handler.pull("master");
        handler.commit("commit11");
        //        when
        File f1 = createAndAddFile(1);
        File f2 = createAndAddFile(2);
        File f3 = createAndAddFile(3);
        handler.commit("commit2");
        //        when
        PushResponse actual = handler.push("master");
        //then
        System.out.println(actual.messages());
        assertThat(actual.isSuccessful()).isTrue();
    }

    @Test
    public void pushTag() throws IOException, GitAPIException {
        //when
        FileUtils.forceDelete(new File(temporaryFolder.getRoot(), "git/scratch"));
        handler.cloneRepo(gitDir, "master");
        //        then
        handler.commit("commit1");
        handler.pull("master");
        handler.commit("commit11");
        //        when
        File f1 = createAndAddFile(1);
        File f2 = createAndAddFile(2);
        File f3 = createAndAddFile(3);
        handler.commit("commit2");
        Date d = new Date();
        handler.tag("tag" + d.getTime());
        //        when
        PushResponse actual = handler.pushTags("master");
        //then
        System.out.println(actual.messages());
        assertThat(actual.isSuccessful()).isTrue();
    }

    @Test
    public void developCommits() throws Exception {
        // given
        File f1 = createAndAddFile(1);
        File f2 = createAndAddFile(2);
        File f3 = createAndAddFile(3);
        handler.commit("commit1");
        handler.createBranch("develop");


        //when
        List<Ref> tags = handler.getTags();
        ImmutableSet<RevCommit> masterCommits = handler.extractMasterCommits();
        ImmutableSet<RevCommit> developCommits = handler.extractDevelopCommits();


        //then
        assertThat(developCommits.size()).isEqualTo(1);
        assertThat(masterCommits.size()).isEqualTo(1);
    }


    @Test
    public void getOriginAndOriginRepoName() throws Exception {
        //given
        projectDir = new File("/home/david/git/krail");
        handler.setProjectDir(projectDir);

        //when
        final String origin = handler.getOrigin();
        final String originRepoName = handler.getOriginRepoBaseUrl();

        //then
        assertThat(origin).isNotNull();
        assertThat(origin).isEqualTo("https://github.com/davidsowerby/krail.git");
        assertThat(originRepoName).isEqualTo("https://github.com/davidsowerby/krail");

    }
}