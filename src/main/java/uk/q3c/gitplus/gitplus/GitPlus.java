package uk.q3c.gitplus.gitplus;

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
        try {
            if (configuration.isCreateLocalRepo()) {
                createLocalRepo();
            } else if (configuration.isCloneRemoteRepo()) {
                cloneRemote();
            }
            if (configuration.getRemoteRepoFullName() == null) {
                String origin = gitLocal.getOrigin();
                if (origin != null) {
                    GitRemote.Provider provider = configuration.getRemoteProvider();
                    GitRemoteFactory remoteFactory = configuration.getGitRemoteFactory();
                    configuration.remoteRepoFullName(remoteFactory.repoFullNameFromCloneUrl(provider, origin));
                }
            }
        } catch (Exception e) {
            throw new GitPlusException("Failed to create or verify repository", e);
        }
        return this;


    }

    public void createRemoteRepo() throws IOException {
        getGitRemote().createRepo();
    }


    public void cloneRemote() {
        gitLocal.cloneRemote();
    }

    private void createLocalRepo() throws IOException, GitAPIException {
        log.debug("creating local repo");
        gitLocal.createLocalRepo();
        if (configuration.isCreateProject()) {
            configuration.getProjectCreator()
                         .execute(configuration.getProjectDir());
        }
        if (configuration.isCreateRemoteRepo()) {
            createRemoteRepo();
            gitLocal.push(gitRemote, true);
        }
    }


    public GitRemote getGitRemote() throws IOException {
        if (gitRemote == null) {
            gitRemote = configuration.getGitRemoteFactory()
                                     .create(configuration);
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