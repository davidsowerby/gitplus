package uk.q3c.build.gitplus.gitplus;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import uk.q3c.build.gitplus.changelog.ChangeLog;
import uk.q3c.build.gitplus.changelog.ChangeLogConfiguration;
import uk.q3c.build.gitplus.local.GitCommit;
import uk.q3c.build.gitplus.local.GitLocal;
import uk.q3c.build.gitplus.local.Tag;
import uk.q3c.build.gitplus.remote.GitRemote;
import uk.q3c.build.gitplus.remote.GitRemoteFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Brings together<ol>
 * <li>a {@link GitLocal} instance representing a local Git repo,</li>
 * <li>a {@link GitRemote} instance representing a remote, hosted Git with issues</li>
 * <li>a further, optional {@link GitLocal} instance to represent a wiki repo associated with the main code repo.  This may not apply to all remote ervice
 * providers</li>
 * <p>
 * A default configuration is created when getConfiguration() is called, which must then be configured as require, for example: <p>
 * GitPlus gitPlus = new GitPlus()<br>
 * getPlus.getConfiguration().createLocal(true)<br><br>
 * {@link #gitLocal} and {@link #wikiLocal} are set by GitLocalProvider instance in configuration
 * Created by David Sowerby on 12/01/15.
 */
public class GitPlus implements AutoCloseable {
    public static final String MASTER_BRANCH = "master";
    public static final String DEVELOP_BRANCH = "develop";
    public static final String REMOTE = "remote";
    public static final String ORIGIN = "origin";
    public static final String URL = "url";
    private static Logger log = getLogger(GitPlus.class);
    private GitLocal gitLocal;
    private GitLocal wikiLocal;
    private GitRemote gitRemote;

    private GitPlusConfiguration configuration;
    private ChangeLog changelog;

    /**
     * Closes {@link GitLocal} instances to free up resources
     */
    @Override
    public void close() throws IOException {
        if (gitLocal != null) {
            gitLocal.close();
        }

        if (wikiLocal != null) {
            wikiLocal.close();
        }
    }

    /**
     * The main entry point when using GitPlus to create repos.  Creates local and/or remote repos depending on configuration settings, and/or reads existing
     * repo settings.
     *
     * @throws GitPlusException if anything fails
     */
    public GitPlus createOrVerifyRepos() {

        try {
            if (getConfiguration().isCreateLocalRepo() && configuration.isCreateRemoteRepo()) {
                createFullSetup();
                return this;
            }
            if (configuration.isCloneRemoteRepo()) {
                cloneRemote();
            } else if (configuration.isCreateLocalRepo()) {
                createLocalRepo();
            } else {
                verifyRemoteFromLocal();  // We are not creating anything just use it as it is
            }
        } catch (Exception e) {
            throw new GitPlusException("Failed to create or verify repository", e);
        }
        return this;
    }

    public GitPlusConfiguration getConfiguration() {
        if (configuration == null) {
            configuration = new GitPlusConfiguration();
        }
        return configuration;
    }

    public void verifyRemoteFromLocal() throws IOException {
        if (getConfiguration().getRemoteRepoFullName() == null) {
            String origin = getGitLocal().getOrigin();
            if (origin != null) {
                GitRemoteFactory remoteFactory = configuration.getGitRemoteFactory();
                configuration.remoteRepoFullName(remoteFactory.fullRepoNameFromCloneUrl(origin));
            }
        }
    }

    /**
     * Creates local repo, remote repo, master and develop branches, a README, and if createProject is true also creates the project (and pushes to remote)
     * as well.  Finishes with 'develop' branch selected
     */
    private void createFullSetup() throws IOException, GitAPIException {
        createLocalRepo();
        addReadmeToLocal();
        gitLocal.commit("Initial commit");
        createRemoteRepo();
        gitLocal.setOrigin();
        push(false);
        gitLocal.createBranch(DEVELOP_BRANCH);
        gitLocal.checkout(DEVELOP_BRANCH);
        push(false);
        if (configuration.isUseWiki()) {
            getWikiLocal().createLocalRepo();
            wikiLocal.setOrigin();
        }
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

    private void createProject() throws IOException {
        log.debug("creating project");
        configuration.getProjectCreator()
                     .execute();
        gitLocal.add(configuration.getProjectDir());
    }

    private void createRemoteRepo() throws IOException {
        getGitRemote().createRepo();
    }

    private void cloneRemote() {
        getGitLocal().cloneRemote();
        if (configuration.isUseWiki()) {
            getWikiLocal().cloneRemote();
        }
    }

    private void createLocalRepo() throws IOException, GitAPIException {
        log.debug("creating local repo");
        getGitLocal().createLocalRepo();
        if (configuration.isCreateProject()) {
            createProject();
        }
    }

    public GitRemote getGitRemote() throws IOException {
        if (gitRemote == null) {
            gitRemote = getConfiguration().getGitRemoteFactory()
                                          .createRemoteInstance(configuration);
        }
        return gitRemote;
    }

    public String getProjectName() {
        return getConfiguration().getProjectName();
    }

    public File getProjectDir() {
        return getConfiguration().getProjectDir();
    }

    public Repository getLocalRepo() throws IOException {
        return getGitLocal().getGit()
                            .getRepository();
    }

    public String getRemoteHtmlUrl() throws IOException {
        return getGitRemote().getHtmlUrl();
    }

    public String getRemoteTagUrl() throws IOException {
        return getGitRemote().getTagUrl();
    }

    public List<Tag> getTags() {
        return getGitLocal().getTags();
    }

    public List<GitCommit> extractDevelopCommits() {
        return getGitLocal().extractDevelopCommits();
    }

    public List<GitCommit> extractMasterCommits() {
        return gitLocal.extractMasterCommits();
    }

    public GitLocal getGitLocal() {
        if (gitLocal == null) {
            gitLocal = configuration.getGitLocalProvider()
                                    .get(configuration);
        }
        return gitLocal;
    }

    public GitLocal getWikiLocal() {
        if (wikiLocal == null) {
            wikiLocal = configuration.getGitLocalProvider()
                                     .get(configuration);
            wikiLocal.configureForWiki();
        }
        return wikiLocal;
    }

    public void push(boolean pushTags) throws IOException {
        getGitLocal().push(getGitRemote(), pushTags);
    }

    public void pushWiki() throws IOException {
        getWikiLocal().push(getGitRemote(), false);
    }

    public ChangeLog getChangelog() {
        if (changelog == null) {
            changelog = new ChangeLog(this);
        }
        return changelog;
    }

    public void setChangelog(ChangeLog changelog) {
        this.changelog = changelog;
    }

    /**
     * Generates a changelog using {@link ChangeLogConfiguration} passed in with {@link GitPlusConfiguration}, or with default {@link ChangeLogConfiguration}
     * if none supplied
     *
     * @return a File object referencing the generated output
     * @throws IOException if failure occurs during generation
     */
    public File generateChangeLog() throws IOException {
        return getChangelog().createChangeLog();
    }

    /**
     * Returns the hash for the latest commit on the develop branch.  Ideally this information would be replaced by the payload from a webhook, as there is
     * always a risk that the call to this method may be incorrect (that is, the push which made a change, is followed quickly by a second push)
     */
    public String latestRemoteDevelopCommit() throws IOException {
        return getGitRemote().latestDevelopCommit();
    }

    /**
     * Generates a changelog using {@code changeLogConfiguration}
     *
     * @return a File object referencing the generated output
     * @throws IOException if failure occurs during generation
     */
    public File generateChangeLog(@Nonnull ChangeLogConfiguration changeLogConfiguration) throws IOException {
        checkNotNull(changeLogConfiguration);
        getChangelog().setConfiguration(changeLogConfiguration);
        return changelog.createChangeLog();
    }

    /**
     * Generates a changelog using {@link ChangeLogConfiguration} passed in with {@link GitPlusConfiguration}, or with default {@link ChangeLogConfiguration}
     * if none supplied, modified by {@code outputTarget}.
     *
     * @return a File object referencing the generated output
     * @throws IOException if failure occurs during generation
     */
    public File generateChangeLog(@Nonnull ChangeLogConfiguration.OutputTarget outputTarget) throws IOException {
        checkNotNull(outputTarget);
        getChangelog().getConfiguration()
                      .outputTarget(outputTarget);
        return changelog.createChangeLog();
    }

    /**
     * Generates a changelog using {@link ChangeLogConfiguration} passed in with {@link GitPlusConfiguration}, or with default {@link ChangeLogConfiguration}
     * if none supplied, modified by {@code outputFile}.  The configuration output target is set to USE_FILE_SPEC
     *
     * @return a File object referencing the generated output
     * @throws IOException if failure occurs during generation
     */
    public File generateChangeLog(@Nonnull File outputFile) throws IOException {
        checkNotNull(outputFile);
        getChangelog().getConfiguration()
                      .outputFile(outputFile)
                      .outputTarget(ChangeLogConfiguration.OutputTarget.USE_FILE_SPEC);
        return changelog.createChangeLog();
    }
}