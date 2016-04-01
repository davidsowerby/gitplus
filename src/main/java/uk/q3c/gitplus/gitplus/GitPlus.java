package uk.q3c.gitplus.gitplus;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import uk.q3c.gitplus.local.GitCommit;
import uk.q3c.gitplus.local.GitLocal;
import uk.q3c.gitplus.local.GitLocalProvider;
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
 * Brings together<ol>
 * <li>a {@link GitLocal} instance representing a local Git repo,</li>
 * <li>a {@link GitRemote} instance representing a remote, hosted Git with issues</li>
 * <li>a further, optional {@link GitLocal} instance to represent a wiki repo associated with the main code repo.  This may not apply to all remote ervice
 * providers</li>
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
    private GitLocal wikiLocal;
    private GitRemote gitRemote;
    private boolean reposVerified = false;

    private GitPlusConfiguration configuration;

    public GitPlus(@Nonnull GitPlusConfiguration configuration, @Nonnull GitLocalProvider gitLocalProvider) {
        checkNotNull(configuration);
        checkNotNull(gitLocalProvider);
        configuration.validate();
        this.configuration = new GitPlusConfiguration(configuration); // copy so that we can modify
        this.gitLocal = gitLocalProvider.get(configuration);
        if (configuration.isUseWiki()) {
            this.wikiLocal = gitLocalProvider.get(configuration);
            wikiLocal.configureForWiki();
        }

    }



    /**
     * Closes {@link GitLocal} instance to free up resources
     */
    @Override
    public void close() throws IOException {
        gitLocal.close();
        if (wikiLocal != null) {
            wikiLocal.close();
        }
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
            if (configuration.isCloneRemoteRepo()) {
                cloneRemote();
            }
            if (configuration.isCreateLocalRepo()) {
                createLocalRepo();
            }
            // We are not creating anything just use it as it is
            verifyRemoteFromLocal();
            reposVerified = true;
        } catch (Exception e) {
            throw new GitPlusException("Failed to create or verify repository", e);
        }
        return this;
    }

    public GitPlusConfiguration getConfiguration() {
        return configuration;
    }


    private void verifyRemoteFromLocal() throws IOException {
        if (configuration.getRemoteRepoFullName() == null) {
            String origin = gitLocal.getOrigin();
            if (origin != null) {
                GitRemoteFactory remoteFactory = configuration.getGitRemoteFactory();
                configuration.remoteRepoFullName(remoteFactory.fullRepoNameFromCloneUrl(origin));
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
        gitLocal.commit("Initial commit");
        createRemoteRepo();
        gitLocal.setOrigin();
        gitLocal.push(getGitRemote(), false);
        gitLocal.createBranch(DEVELOP_BRANCH);
        gitLocal.checkout(DEVELOP_BRANCH);
        gitLocal.push(getGitRemote(), false);
        if (configuration.isUseWiki()) {
            wikiLocal.createLocalRepo();
            wikiLocal.setOrigin();
        }
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
        log.debug("creating project");
        configuration.getProjectCreator()
                     .execute(configuration.getProjectDir());
        gitLocal.add(configuration.getProjectDir());
    }

    private void createRemoteRepo() throws IOException {
        getGitRemote().createRepo();
    }

    private void cloneRemote() {
        gitLocal.cloneRemote();
        if (configuration.isUseWiki()) {
            wikiLocal.cloneRemote();
        }
    }

    private void createLocalRepo() throws IOException, GitAPIException {
        log.debug("creating local repo");
        gitLocal.createLocalRepo();
        if (configuration.isCreateProject()) {
            createProject();
        }
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

    public GitLocal getGitLocal() {
        return gitLocal;
    }

    public GitLocal getWikiLocal() {
        return wikiLocal;
    }
}