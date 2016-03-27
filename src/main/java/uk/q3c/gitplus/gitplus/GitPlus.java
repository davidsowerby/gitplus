package uk.q3c.gitplus.gitplus;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import uk.q3c.gitplus.local.GitCommit;
import uk.q3c.gitplus.local.GitLocal;
import uk.q3c.gitplus.local.Tag;
import uk.q3c.gitplus.remote.GitRemote;
import uk.q3c.gitplus.remote.GitRemoteFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A thin wrapper around Git to make some of the commands either simpler or just more direct and relevant
 * <p>
 * Created by David Sowerby on 12/01/15.
 */
public class GitPlus implements AutoCloseable {
    public static final String MASTER_BRANCH = "master";
    public static final String DEVELOP_BRANCH = "develop";
    public static final String REMOTE = "remote";
    public static final String ORIGIN = "origin";
    public static final String URL = "url";
    private static Logger log = getLogger(GitPlus.class);
    private final GitLocal gitLocal;
    private GitRemote gitRemote;
    private boolean reposVerified = false;

    private GitPlusConfiguration configuration;

    public GitPlus(@Nonnull GitPlusConfiguration configuration, @Nonnull GitLocal gitLocal) {
        checkNotNull(configuration);
        checkNotNull(gitLocal);
        this.gitLocal = gitLocal;
        this.configuration = configuration;
        configuration.validate();
    }


    /**
     * Closes {@link GitLocal} instance to free up resources
     */
    @Override
    public void close() throws IOException {
        gitLocal.close();
    }


    /**
     * Creates local and/or remote repos depending on configuration settings, and/or reading existing repo settings.  Call before calling other methods to
     * ensure configuration is complete.<p>
     * The first stage is to evaluate the various scenarios from having nothing set up (and therefore needing to create a
     * project and repo) to having everything already set up.
     * <p>
     * The configuration has already been validated by a call in the constructor
     *
     * @throws GitPlusException if construction fails
     */
    public GitPlus createOrVerifyRepos() {
        if (reposVerified) {
            log.info("Called createOrVerifyRepos, but repos already verified, no action taken");
            return this;
        }
        try {
            if (configuration.isCreateLocalRepo() && configuration.isCreateRemoteRepo()) {
                createFullSetup();
                return this;
            }
            createOrCloneLocal();
            verifyRemoteFromLocal();
            reposVerified = true;
        } catch (Exception e) {
            throw new GitPlusException("Failed to create or verify repository", e);
        }
        return this;
    }

    private void createOrCloneLocal() throws IOException, GitAPIException {
        if (configuration.isCreateLocalRepo()) {
            createLocalRepo();
            if (configuration.isCreateProject()) {
                createProject();
            }
        } else if (configuration.isCloneRemoteRepo()) {
            cloneRemote();
        }
    }

    private void verifyRemoteFromLocal() throws IOException {
        if (configuration.getRemoteRepoFullName() == null) {
            String origin = gitLocal.getOrigin();
            if (origin != null) {
                GitRemote.ServiceProvider serviceProvider = configuration.getRemoteServiceProvider();
                GitRemoteFactory remoteFactory = configuration.getGitRemoteFactory();
                configuration.remoteRepoFullName(remoteFactory.repoFullNameFromCloneUrl(serviceProvider, origin));
            }
        }
    }

    /**
     * Creates local repo, remote repo, master and develop branches, a README, and if createProject is true also creates the project (and pushes to remote)
     * as well.
     */
    private void createFullSetup() throws IOException, GitAPIException {
        createLocalRepo();
        addReadmeToLocal();
        if (configuration.isCreateProject()) {
            createProject();
        }
        gitLocal.commit("Initial commit");
        createRemoteRepo();
        gitLocal.setOrigin(gitRemote);
        gitLocal.push(getGitRemote(), false);
        gitLocal.createBranch(DEVELOP_BRANCH);
        gitLocal.checkout(DEVELOP_BRANCH);
        gitLocal.push(getGitRemote(), false);
        reposVerified = true;
    }

    /**
     * Creates a README file with just he project name in it, and adds the file to Git
     */
    private void addReadmeToLocal() throws IOException {
        File f = new File(configuration.getProjectDir(), "README.md");
        List<String> lines = new ArrayList<>();
        lines.add("# " + configuration.getProjectName());
        FileUtils.writeLines(f, lines);
        gitLocal.add(f);
    }


    private void createProject() {
        configuration.getProjectCreator()
                     .execute(configuration.getProjectDir());
        gitLocal.add(configuration.getProjectDir());
    }

    private void createRemoteRepo() throws IOException {
        getGitRemote().createRepo();
    }

    private void cloneRemote() {
        gitLocal.cloneRemote();
    }

    private void createLocalRepo() throws IOException, GitAPIException {
        log.debug("creating local repo");
        gitLocal.createLocalRepo();
    }


    public GitRemote getGitRemote() throws IOException {
        if (gitRemote == null) {
            gitRemote = configuration.getGitRemoteFactory()
                                     .createRemoteInstance(configuration);
        }
        return gitRemote;
    }

    public String getProjectName() {
        return configuration.getProjectName();
    }

    public File getProjectDir() {
        return configuration.getProjectDir();
    }

    public Repository getLocalRepo() throws IOException {
        return gitLocal.getGit()
                       .getRepository();
    }

    public String getRemoteHtmlUrl() throws IOException {
        return getGitRemote().getHtmlUrl();
    }

    public String getRemoteTagUrl() throws IOException {
        return getGitRemote().getTagUrl();
    }


    public List<Tag> getTags() {
        return gitLocal.getTags();
    }

    public Set<GitCommit> extractDevelopCommits() {
        return gitLocal.extractDevelopCommits();
    }

    public Set<GitCommit> extractMasterCommits() {
        return gitLocal.extractMasterCommits();
    }
}