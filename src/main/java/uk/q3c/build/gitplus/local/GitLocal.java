package uk.q3c.build.gitplus.local;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.slf4j.Logger;
import uk.q3c.build.gitplus.gitplus.GitPlus;
import uk.q3c.build.gitplus.gitplus.GitPlusConfiguration;
import uk.q3c.build.gitplus.remote.GitRemote;
import uk.q3c.build.gitplus.remote.GitRemoteFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A thin wrapper around Git to make some of the commands either simpler or just more direct and relevant to the task of using within Gradle
 */
public class GitLocal implements AutoCloseable {
    private static Logger log = getLogger(GitLocal.class);

    private Git git;
    private GitPlusConfiguration configuration;

    public GitLocal(@Nonnull GitPlusConfiguration configuration) {
        checkNotNull(configuration);
        this.configuration = new GitPlusConfiguration(configuration); //copy so that we can modify
    }

    /**
     * Enables use of mocked Git for testing
     *
     * @param git           instance of Git
     * @param configuration configuration object, which must have had validate() called before using here
     */
    public GitLocal(@Nonnull Git git, @Nonnull GitPlusConfiguration configuration) {
        checkNotNull(configuration);
        checkNotNull(git);
        this.git = git;
        this.configuration = configuration;
    }

    /**
     * Equivalent to 'git init' from the command line.  Initialises {@code projectDir} for Git
     */
    public void init() {

        Repository repo = null;
        try {
            File gitDir = new File(configuration
                    .getProjectDir(), ".git");
            repo = new FileRepository(gitDir);
            repo.create();
        } catch (Exception e) {
            throw new GitLocalException("Unable to init " + configuration.getProjectDir(), e);

        } finally {
            if (repo != null) {
                // even if successful projectDir creates its own Git instance, so this instance to close
                repo.close();
            }
        }


    }

    /**
     * Closes Git instance to free up resources, and sets {@link #git} to null so it cannot be re-used
     */
    @Override
    public void close() throws IOException {
        if (git == null) {
            return;
        }
        getGit().close();
        git = null;
    }

    /**
     * Clones the remote repo to local.  The clone url is taken from {@link #configuration}.  If a local directory already exists which would have to be
     * overwritten (presumably because of an earlier clone), the outcome is determined by {@link GitPlusConfiguration#getCloneExistsResponse()}:<ol>
     * <li>DELETE - deletes the local copy and clones from remote</li>
     * <li>PULL - executes a Git 'pull' instead of a clone</li>
     * <li>EXCEPTION - throws a GitLocalException</li>
     * <p>
     * </ol>
     */
    public void cloneRemote() {
        log.debug("clone requested");

        try {
            File localDir = configuration.getProjectDir();
            if (localDir.exists()) {
                log.debug("local copy (assumed to be clone) already exists");
                switch (configuration.getCloneExistsResponse()) {
                    case DELETE:
                        //this will throw exception if denied
                        deleteFolderIfApproved(localDir);
                        doClone(localDir);
                        break;

                    case PULL:
                        pull();
                        break;
                    case EXCEPTION:
                    default:
                        log.debug("Exception thrown as configured");
                        throw new GitLocalException("Git clone called, when Git local directory already exists");

                }
            } else {
                doClone(localDir);
            }


        } catch (Exception e) {
            throw new GitLocalException("Unable to clone " + configuration
                    .getRemoteRepoHtmlUrl(), e);
        }
    }

    private void doClone(File localDir) throws GitAPIException {
        log.debug("cloning remote from: {}", configuration
                .getRemoteRepoHtmlUrl());
        CloneCommand cloneCommand = Git.cloneRepository()
                                       .setURI(configuration.getCloneUrl())
                                       .setDirectory(localDir);
        cloneCommand.call();
    }

    private void deleteFolderIfApproved(File localDir) throws IOException {
        if (configuration.getFileDeleteApprover()
                         .approve(localDir)) {
            FileUtils.forceDelete(localDir);
            log.debug("'{}' deleted", localDir);
        } else {
            log.debug("Delete of '{}' not approved", localDir);
            throw new GitLocalException("Delete of directory not approved: " + localDir.getAbsolutePath());
        }
    }

    public void pull() {
        try {
            final PullCommand pull = git.pull();
            pull.call();
        } catch (Exception e) {
            throw new GitLocalException("Pull failed", e);
        }
    }

    public void createLocalRepo() {
        try {
            log.debug("creating local and remote repo for project: {}", configuration.getProjectName());
            File projectDir = configuration.getProjectDir();
            FileUtils.forceMkdir(projectDir);
            init();
        } catch (Exception e) {
            throw new GitLocalException("Unable to create local repo", e);
        }
    }

    public void checkout(@Nonnull String branchName) {
        checkNotNull(branchName);
        try {
            getGit()
                    .checkout()
                    .setName(branchName)
                    .call();
        } catch (Exception e) {
            throw new GitLocalException("Unable checkout branch " + branchName, e);
        }
    }

    public void createBranch(@Nonnull String branchName) {
        checkNotNull(branchName);
        try {
            getGit().branchCreate()
                    .setName(branchName)
                    .call();
        } catch (Exception e) {
            throw new GitLocalException("Unable to create branch " + branchName, e);
        }
    }

    /**
     * Add file (or recursively add a directory).  JGit behaves slightly differently than command line Git.  This call will init() a repo if it has not been
     * already.
     *
     * @param file the file to add
     * @throws GitLocalException if the action fails
     */
    public void add(@Nonnull File file) {
        checkNotNull(file);
        try {
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
            getGit().add()
                    .addFilepattern(file.getName())
                    .call();
        } catch (Exception e) {
            throw new GitLocalException("Unable to add file to git: " + file, e);
        }
    }

    public void commit(@Nonnull String message) {
        checkNotNull(message);
        try {
            git.commit()
               .setMessage(message)
               .call();
        } catch (Exception e) {
            throw new GitLocalException("Unable to process Git request to commit", e);
        }
    }

    /**
     * Returns a list of branches in the repo, or an empty list if there are no branches
     *
     * @return a list of branches in the repo, or an empty list if there are no branches
     */
    public List<String> branches() {
        try {
            if (getGit().getRepository()
                        .isBare()) {
                throw new GitLocalException("Repo has o working tree");
            }
            final List<Ref> refs = getGit().branchList()
                                           .call();
            List<String> branchNames = new ArrayList<>();
            refs.forEach(r -> branchNames.add(r.getName()
                                               .replace("refs/heads/", "")));
            return branchNames;
        } catch (Exception e) {
            throw new GitLocalException("Unable to list branches ", e);
        }
    }

    public Git getGit() throws IOException {
        if (git == null) {
            openRepository();
        }
        return git;
    }

    public String currentBranch() {
        try {
            if (getGit().getRepository()
                        .isBare()) {
                throw new GitLocalException("Repo has o working tree");
            }
            return getGit().getRepository()
                           .getBranch();
        } catch (Exception e) {
            throw new GitLocalException("Unable to get current branch ", e);
        }
    }

    public Status status() {
        try {
            return getGit().status()
                           .call();
        } catch (Exception e) {
            throw new GitLocalException("Git status() failed", e);
        }
    }

    /**
     * Returns the URL for the remote called 'origin'.  Note that it has the '.git' suffix appended.
     *
     * @return the URL for the remote called 'origin'.  Note that it has the '.git' suffix appended.
     * @throws GitLocalException of the origin does not exist, or for any other failure
     */
    public String getOrigin() throws IOException {
        try {
            final StoredConfig config = getGit().getRepository()
                                                .getConfig();
            Set<String> remotes = config.getSubsections(GitPlus.REMOTE);
            if (!remotes.contains(GitPlus.ORIGIN)) {
                throw new GitLocalException("No origin has been defined for " + configuration.getProjectDir());
            }
            return config.getString(GitPlus.REMOTE, GitPlus.ORIGIN, GitPlus.URL);
        } catch (Exception e) {
            throw new GitLocalException("Unable to get the origin", e);
        }
    }

    public void setOrigin(@Nonnull GitRemote gitRemote) {
        checkNotNull(gitRemote);
        try {
            String originUrl = gitRemote.getCloneUrl();
            setOrigin(originUrl);
        } catch (Exception e) {
            throw new GitLocalException("Unable to set origin", e);
        }
    }

    public void setOrigin(@Nonnull String origin) {
        checkNotNull(origin);
        try {
            StoredConfig config = getGit().getRepository()
                                          .getConfig();
            config.setString("remote", "origin", "url", origin);
            config.save();

        } catch (Exception e) {
            throw new GitLocalException("Unable to set origin", e);
        }
    }

    public void setOrigin() {
        setOrigin(configuration.getCloneUrl());
    }

    /**
     * Pushes the currently checked out branch to the remote origin specified in {@link #configuration#getRemoteRepoUrl()}
     */
    public PushResponse push(@Nonnull GitRemote gitRemote, boolean tags) {
        checkNotNull(gitRemote);

        try {
            PushCommand pc = getGit().push()
                                     .setCredentialsProvider(gitRemote.getCredentialsProvider());

            if (tags) {
                pc.setPushTags();
            }
            Iterable<PushResult> results = pc.call();
            PushResponse response = new PushResponse();
            for (PushResult pushResult : results) {
                response.add(pushResult);
            }
            return response;

        } catch (Exception e) {
            throw new GitLocalException("Unable to push", e);
        }
    }

    private void openRepository() throws IOException {
        File f = new File(configuration.getProjectDir(), ".git");
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder.setGitDir(f)
                                 .readEnvironment() // scan environment GIT_* variables
                                 .findGitDir() // scan up the file system tree
                                 .build();
        git = new Git(repo);


    }

    /**
     * Returns all the tags from the current repo, in a slightly more digestible form than the JGit tags.  Works with either annotated or lightweight tags.
     *
     * @return all the tags from the current repo, in a slightly more digestible form than the JGit tags.  Works with either annotated or lightweight tags.
     */
    public List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();
        try {
            List<Ref> refs = getGit().tagList()
                                     .call();
            Repository repo = getGit().getRepository();
            RevWalk walk = new RevWalk(repo);
            for (Ref ref : refs) {
                RevObject revObject = walk.parseAny(ref.getObjectId());
                String tagName = ref.getName()
                                    .replace("refs/tags/", "");
                Tag tag = new Tag(tagName);

                //annotated tag, get the additional info
                if (revObject instanceof RevTag) {
                    RevTag revTag = (RevTag) revObject;
                    tag.releaseDate(extractDateFromIdent(revTag.getTaggerIdent()));
                    RevCommit codeCommit = (RevCommit) revTag.getObject();
                    walk.parseCommit(codeCommit);
                    tag.commitDate(extractDateFromIdent(codeCommit.getCommitterIdent()))
                       .taggerIdent(revTag.getTaggerIdent())
                       .fullMessage(revTag.getFullMessage())
                       .tagType(Tag.TagType.ANNOTATED)
                       .commit(new GitCommit(codeCommit));


                } else {
                    //lightweight tag has not tagger Ident, uses the committer ident
                    RevCommit revCommit = (RevCommit) revObject;
                    tag.releaseDate(extractDateFromIdent(revCommit.getCommitterIdent()))
                       .commitDate(extractDateFromIdent(revCommit.getCommitterIdent()))
                       .noTagMessage()
                       .taggerIdent(revCommit.getCommitterIdent())
                       .tagType(Tag.TagType.LIGHTWEIGHT)
                       .commit(new GitCommit(revCommit));
                }
                tags.add(tag);
            }
            return tags;
        } catch (Exception e) {
            throw new GitLocalException("Unable to read Git status" + configuration.getProjectDir()
                                                                                   .getAbsolutePath(), e);
        }
    }

    private ZonedDateTime extractDateFromIdent(PersonIdent personIdent) {
        Date when = personIdent.getWhen();
        return when.toInstant()
                   .atZone(personIdent.getTimeZone()
                                      .toZoneId());
    }

    public ImmutableList<GitCommit> extractDevelopCommits() {
        return extractCommitsFor("refs/heads/develop");
    }

    private ImmutableList<GitCommit> extractCommitsFor(String ref) {
        try {
            Repository repo = getGit().getRepository();
            RevWalk walk = new RevWalk(repo);
            Ref ref1 = repo.getRef(ref);
            RevCommit headCommit = walk.parseCommit(ref1.getObjectId());
            Iterable<RevCommit> allCommits = git.log()
                                                .all()
                                                .call();
            LinkedHashSet<GitCommit> branchCommits = new LinkedHashSet<>();
            for (RevCommit commit : allCommits) {
                RevCommit candidate = walk.parseCommit(commit);
                if (walk.isMergedInto(candidate, headCommit)) {
                    walk.parseBody(candidate);
                    GitCommit gitCommit = new GitCommit(commit);
                    branchCommits.add(gitCommit);
                }
            }
            return ImmutableList.copyOf(branchCommits);
        } catch (Exception e) {
            throw new GitLocalException("Reading commits for " + ref + " failed", e);
        }
    }

    public ImmutableList<GitCommit> extractMasterCommits() {
        return extractCommitsFor("refs/heads/master");
    }

    /**
     * Adds {@code tag} to the most recent commit.  This will add an annotated tag.  See also {@link #tagLightweight}
     *
     * @param tag the tag to apply
     */
    public void tag(String tag) {
        try {
            TagCommand tagCommand = git.tag();
            Date tagDate = new Date();
            PersonIdent personalIdent = new PersonIdent(configuration.getTaggerName(), configuration.getTaggerEmail());
            tagCommand.setMessage("Released at version " + tag)
                      .setAnnotated(true)
                      .setName(tag)
                      .setTagger(new PersonIdent(personalIdent, tagDate));
            tagCommand.call();

        } catch (Exception e) {
            throw new GitLocalException("Unable to tag" + configuration.getProjectDir()
                                                                       .getAbsolutePath(), e);
        }
    }

    /**
     * Adds {@code tag} to the most recent commit.  This will add a lightweight tag.  See also {@link #tag}
     *
     * @param tag the tag to apply
     */
    public void tagLightweight(String tag) {
        try {
            TagCommand tagCommand = git.tag();
            tagCommand.setAnnotated(false)
                      .setName(tag)
                      .call();
        } catch (Exception e) {
            throw new GitLocalException("Unable to tag" + configuration.getProjectDir()
                                                                       .getAbsolutePath(), e);
        }
    }

    /**
     * Adjusts the {@link #configuration} for use with a wiki repo - these adjustments depend on the remote provider, and therefore use the factory from
     * {@link GitPlusConfiguration#getGitRemoteFactory()} to provide the conversions
     */
    public void configureForWiki() {
        GitRemoteFactory factory = configuration.getGitRemoteFactory();
        String coreHtmlUrl = configuration.getRemoteRepoHtmlUrl();
        String wikiCoreHtmlUrl = factory.wikiHtmlUrlFromCoreHtmlUrl(coreHtmlUrl);
        String wikiCloneUrl = factory.wikiCloneUrlFromCoreHtmLUrl(coreHtmlUrl);
        String projectName = configuration.getProjectName() + ".wiki";
        File projectDir = new File(configuration.getProjectDir()
                                                .getParentFile(), projectName);
        configuration.remoteRepoHtmlUrl(wikiCoreHtmlUrl)
                     .projectName(projectName)
                     .createRemoteRepo(false)
                     .cloneRemoteRepo(true)
                     .cloneUrl(wikiCloneUrl)
                     .projectDir(projectDir);

    }

    public GitPlusConfiguration getConfiguration() {
        return configuration;
    }

    public File getProjectDir() {
        return configuration.getProjectDir();
    }
}