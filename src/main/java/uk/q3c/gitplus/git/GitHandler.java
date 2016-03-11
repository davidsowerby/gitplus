package uk.q3c.gitplus.git;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.ListUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by David Sowerby on 12/01/15.
 */
public class GitHandler {

    private UsernamePasswordCredentialsProvider credentialsProvider;
    private String remoteUrl;
    private File projectDir;

    public GitHandler() {
        Properties properties = new Properties();
        File home = new File(System.getProperty("user.home"));
        File gradle = new File(home, "gradle");
        File gradleProperties = new File(gradle, "gradle.properties");
        try {
            FileInputStream fis = new FileInputStream(gradleProperties);
            properties.load(fis);
            String userName = (String) properties.get("github-username");
            String password = (String) properties.get("github-password");
            remoteUrl = (String) properties.get("github-url");
            if (!remoteUrl.endsWith("/")) {
                remoteUrl = remoteUrl + "/";
            }
            credentialsProvider = new UsernamePasswordCredentialsProvider(userName, password);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load gradle.properties");
        }
    }

    public File getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir;
    }

    public boolean developIsAheadOfMaster(File projectDir) {
        CommitLists lists = extractCommitLists(projectDir);
        return lists.developIsAheadOfMaster();
    }

    public ImmutableSet<RevCommit> extractCommitsFor(String ref) {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            RevWalk walk = new RevWalk(repo);
            Ref ref1 = repo.getRef(ref);
            RevCommit headCommit = walk.parseCommit(ref1.getObjectId());
            Iterable<RevCommit> allCommits = git.log()
                                                .all()
                                                .call();
            LinkedHashSet<RevCommit> branchCommits = new LinkedHashSet<>();
            for (RevCommit commit : allCommits) {
                RevCommit candidate = walk.parseCommit(commit);
                if (walk.isMergedInto(candidate, headCommit)) {
                    branchCommits.add(commit);
                }
            }
            return ImmutableSet.copyOf(branchCommits);
        } catch (Exception e) {
            throw new GitHandlerException("Reading commits for " + ref + " failed", e);
        }
    }

    public ImmutableSet<RevCommit> extractDevelopCommits() {
        return extractCommitsFor("refs/heads/develop");
    }

    public ImmutableSet<RevCommit> extractMasterCommits() {
        return extractCommitsFor("refs/heads/master");
    }

    /**
     * @param projectDir
     * @return
     */
    public CommitLists extractCommitLists(File projectDir) {
        Repository repo = null;
        Set<String> comments = new LinkedHashSet<>();
        String msg = "";
        try {
            repo = openRepository();
            Git git = new Git(repo);
            RevWalk walk = new RevWalk(repo);


            //find commits
            Ref masterHead = repo.getRef("refs/heads/master");
            RevCommit masterHeadCommit = walk.parseCommit(masterHead.getObjectId());

            Ref developHead = repo.getRef("refs/heads/develop");
            if (developHead == null) {
                msg = "You need to have a develop branch";
                throw new GitHandlerException(msg);
            }
            RevCommit developHeadCommit = walk.parseCommit(developHead.getObjectId());


            Iterable<RevCommit> commits = git.log()
                                             .all()
                                             .call();

            List<RevCommit> masterCommits = new ArrayList<>();
            List<RevCommit> developCommits = new ArrayList<>();
            for (RevCommit commit : commits) {

                //At first sight, this line appears not to do anything - but it definitely does
                RevCommit candidate = walk.parseCommit(commit);

                if (walk.isMergedInto(candidate, masterHeadCommit)) {
                    masterCommits.add(commit);
                }

                if (walk.isMergedInto(candidate, developHeadCommit)) {
                    developCommits.add(commit);
                }

            }


            List<RevCommit> developAhead = ListUtils.subtract(developCommits, masterCommits);
            List<RevCommit> masterAhead = ListUtils.subtract(masterCommits, developCommits);

            CommitLists commitLists = new CommitLists();
            commitLists.setDevelopAhead(developAhead);
            commitLists.setMasterAhead(masterAhead);
            return commitLists;


        } catch (Exception e) {
            throw new GitHandlerException("Reading commit lists failed. " + msg, e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    public Repository openRepository() throws IOException {
        File f = new File(projectDir, ".git");
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.setGitDir(f)
                      .readEnvironment() // scan environment GIT_* variables
                      .findGitDir() // scan up the file system tree
                      .build();


    }

    /**
     * Extracts commit comments from those commits in the develop branch which are ahead of the master branch.
     *
     * @param projectDir the project directory
     * @return
     * @throws GitHandlerException if master has any commits ahead of develop
     */
    public Set<String> extractCommitComments(File projectDir) {
        CommitLists commitLists = extractCommitLists(projectDir);
        if (commitLists.getMasterAhead()
                       .size() > 0) {
            throw new GitHandlerException("Master branch has " + commitLists.getMasterAhead()
                                                                            .size() + " commits which are not in " +
                    "develop");
        }


        Set<String> comments = extractComments(commitLists.getDevelopAhead());
        return comments;
    }

    private Set<String> extractComments(List<RevCommit> commits) {
        Set<String> comments = new LinkedHashSet<>();
        commits.forEach(c -> comments.add(c.getFullMessage()));
        return comments;
    }

    public RevCommit headCommit(String branchName) {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            RevWalk walk = new RevWalk(repo);

            //find commits
            Ref head = repo.getRef("refs/heads/" + branchName);
            RevCommit headCommit = walk.parseCommit(head.getObjectId());
            return headCommit;
        } catch (Exception e) {
            throw new GitHandlerException("Unable to process Git request to retrieve head commit", e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    public boolean hasUncommittedChanges(File projectDir) {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            Status status = git.status()
                               .call();
            return status.hasUncommittedChanges();
        } catch (Exception e) {
            throw new GitHandlerException("Unable to process Git request to identify uncommitted changes", e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    public void addFileToGit(String filename) {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            git.add()
               .addFilepattern(filename)
               .call();
        } catch (Exception e) {
            throw new GitHandlerException("Unable to process Git request to add file to Git", e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    /**
     * Merges latest commit with previous commit on the current branch, using Git fixup (and therefore discarding the
     * message from the latest commit).  If there is no previous commit, fails gracefully and leaves things as they
     * are.
     *
     * @param projectDir
     */
    public void fixupLastCommit(File projectDir) {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);

            Iterable<RevCommit> commits = git.log()
                                             .setMaxCount(10)
                                             .all()
                                             .call();

            Iterator<RevCommit> iterator = commits.iterator();


            //this gets the HEAD
            RevCommit headCommit = iterator.next();
            RebaseFixupLastCommit rebaseFixupLastCommit = new RebaseFixupLastCommit(headCommit);
            if (iterator.hasNext()) {
                RevCommit previousCommit = iterator.next();
                //this gets first upstream commit
                RebaseCommand rebase = git.rebase();
                rebase.setUpstream(previousCommit)
                      .runInteractively(rebaseFixupLastCommit)
                      .call();
            }
        } catch (Exception e) {
            throw new GitHandlerException("Unable to process Git request to fixup for: " + projectDir.getName(), e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    public void commit(String message) {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            git.commit()
               .setMessage(message)
               .call();
        } catch (Exception e) {
            throw new GitHandlerException("Unable to process Git request to commit", e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    /**
     * Returns true if {@code projectDir} is a git repo - the test has a limitation, in that it will return false if
     * the
     * directory exists but has no commits
     *
     * @param projectDir
     * @return
     */
    public boolean isRepo(File projectDir) {
        Repository repo = null;
        try {
            repo = openRepository();
            return repo.getObjectDatabase()
                       .exists();
        } catch (Exception e) {
            throw new GitHandlerException("Unable to process Git 'isRepo()'", e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    /**
     * Returns the current branch for the Git folder of {@code projectDir}
     *
     * @param projectDir
     * @return
     */
    public String currentBranch(File projectDir) {
        Repository repo = null;
        try {
            repo = openRepository();
            return repo.getBranch();
        } catch (Exception e) {
            throw new GitHandlerException("Unable to process Git Repo 'getBranch()'", e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    /**
     * Equivalent to 'git init' from the command line
     *
     * @param folder
     */
    public void init(File folder) {
        File gitDir = new File(folder, ".git");
        Repository repo = null;
        try {
            repo = new FileRepository(gitDir);
            repo.create();
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            repo = builder.setGitDir(gitDir)
                          .readEnvironment() // scan environment GIT_* variables
                          .findGitDir() // scan up the file system tree
                          .build();

        } catch (Exception e) {

        } finally {
            if (repo != null) {
                repo.close();
            }
        }

    }

    /**
     * Merge {@code branchName} into the checked out branch of {@code projectDir}
     *
     * @param branchName the branch name to merge into the currently checked out branch
     */
    public void mergeBranch_FastForwardOnly(String branchName) {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            Ref branchHead = repo.getRef("refs/heads/" + branchName);
            MergeCommand mergeCommand = git.merge()
                                           .include(branchHead)
                                           .setFastForward(MergeCommand.FastForwardMode.FF_ONLY);
            mergeCommand.call();
        } catch (Exception e) {
            throw new GitHandlerException("Unable to merge: " + branchName, e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }

    }

    /**
     * Creates a new branch but DOES NOT check it out
     *
     * @param branchName the branch name in short form (for example 'develop')
     */
    public void createBranch(String branchName) {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            git.branchCreate()
               .setName(branchName)
               .call();
        } catch (Exception e) {
            throw new GitHandlerException("Unable to create branch: " + branchName, e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    public void checkout(String branchName) {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            git.checkout()
               .setName(branchName)
               .call();
        } catch (Exception e) {
            throw new GitHandlerException("Unable to create branch: " + branchName, e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }


    public PushResponse pushTags(String branchName) {
        return push(branchName, true);
    }

    private PushResponse push(String branchName, boolean tags) {
        Repository repo = null;
        String localRef = "refs/heads/master";
        String remoteRef = "refs/remotes/origin/master";
        localRef = localRef.replace("master", branchName);
        remoteRef = remoteRef.replace("master", branchName);

        Iterable<PushResult> results = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            PushCommand pc = git.push()
                                .setCredentialsProvider(credentialsProvider);

            if (tags) {
                pc.setPushTags();
            }
            results = pc.call();

        } catch (Exception e) {
            throw new GitHandlerException("Unable to to push: " + branchName, e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }

        PushResponse response = new PushResponse();
        for (PushResult pushResult : results) {
            response.add(pushResult);
        }
        return response;

    }

    public PushResponse push(String branchName) {
        return push(branchName, false);
    }

    public boolean pull(String branchName) {
        Repository repo = null;
        boolean successful = false;
        String refs = "refs/heads/master:refs/remotes/origin/master";
        refs = refs.replace("master", branchName);
        try {
            repo = openRepository();
            Git git = new Git(repo);
            PullCommand pullCommand = git.pull();
            successful = pullCommand.call()
                                    .isSuccessful();

        } catch (Exception e) {
            throw new GitHandlerException("Unable to create branch: " + branchName, e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
        return successful;
    }

    public void cloneRepo(File gitDir, String branchName) {

        Repository repo = null;
        String refs = "refs/heads/master:refs/remotes/origin/master";
        refs = refs.replace("master", branchName);
        try {
            repo = openRepository();
            Git git = new Git(repo);
            CloneCommand cloneCommand = Git.cloneRepository()
                                           .setURI(remoteUrl + projectDir.getName() + ".git")
                                           .setBranch(branchName)
                                           .setDirectory(projectDir);
            cloneCommand.call();

        } catch (Exception e) {
            throw new GitHandlerException("Unable to create branch: " + branchName, e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    public String getOrigin() {
        Repository repo = null;
        try {
            repo = openRepository();
            final StoredConfig config = repo.getConfig();
            Set<String> remotes = config.getSubsections("remote");
            if (!remotes.contains("origin")) {
                throw new GitHandlerException("No origin has been defined for " + projectDir);
            }
            return config.getString("remote", "origin", "url");
        } catch (Exception e) {
            throw new GitHandlerException("Unable to get origin for " + projectDir, e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    public String getOriginRepoBaseUrl() {
        String url = getOrigin();
        return url.substring(0, url.length() - 4);
    }

    public String getOriginRepoName() {
        String baseUrl = getOriginRepoBaseUrl();
        int i = baseUrl.lastIndexOf('/');
        return baseUrl.substring(i + 1);
    }


    public int unTrackedFilesCount() {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            Status status = git.status()
                               .call();
            return status.getUntracked()
                         .size();
        } catch (Exception e) {
            throw new GitHandlerException("Unable to read Git status" + projectDir.getAbsolutePath(), e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    public Set<String> unTrackedFiles() {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            Status status = git.status()
                               .call();
            return status.getUntracked();

        } catch (Exception e) {
            throw new GitHandlerException("Unable to read Git status" + projectDir.getAbsolutePath(), e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    /**
     * Adds {@code tag} to the most recent commit
     *
     * @param tag the tag to apply
     */
    public void tag(String tag) {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            TagCommand tagCommand = git.tag();
            Date tagDate = new Date();
            PersonIdent personalIdent = new PersonIdent("David Sowerby", "X@X");
            tagCommand.setMessage("Released at version " + tag)
                      .setAnnotated(true)
                      .setName(tag)
                      .setTagger(new PersonIdent(personalIdent, tagDate));
            tagCommand.call();

        } catch (Exception e) {
            throw new GitHandlerException("Unable to tag" + projectDir.getAbsolutePath(), e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }

    public List<Ref> getTags() {
        Repository repo = null;
        try {
            repo = openRepository();
            Git git = new Git(repo);
            return git.tagList()
                      .call();

        } catch (Exception e) {
            throw new GitHandlerException("Unable to read Git status" + projectDir.getAbsolutePath(), e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }


    public String getOriginRepoTagUrl() {
        return getOriginRepoBaseUrl() + "/tree";
    }

    public class CommitLists {

        private List<RevCommit> developAhead;
        private List<RevCommit> masterAhead;

        public List<RevCommit> getDevelopAhead() {
            return developAhead;
        }

        public void setDevelopAhead(List<RevCommit> developAhead) {
            this.developAhead = developAhead;
        }

        public List<RevCommit> getMasterAhead() {
            return masterAhead;
        }

        public void setMasterAhead(List<RevCommit> masterAhead) {
            this.masterAhead = masterAhead;
        }

        public boolean developIsAheadOfMaster() {
            return developAhead.size() > 0;
        }
    }

}