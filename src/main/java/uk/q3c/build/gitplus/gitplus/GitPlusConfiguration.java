package uk.q3c.build.gitplus.gitplus;

import com.google.common.collect.ImmutableMap;
import uk.q3c.build.gitplus.changelog.ChangeLogConfiguration;
import uk.q3c.build.gitplus.local.DefaultGitLocalProvider;
import uk.q3c.build.gitplus.local.GitLocalProvider;
import uk.q3c.build.gitplus.remote.DefaultGitRemoteFactory;
import uk.q3c.build.gitplus.remote.DefaultRemoteRepoDeleteApprover;
import uk.q3c.build.gitplus.remote.GitRemote.ServiceProvider;
import uk.q3c.build.gitplus.remote.GitRemoteFactory;
import uk.q3c.build.gitplus.remote.RemoteRepoDeleteApprover;
import uk.q3c.build.gitplus.util.BuildPropertiesLoader;
import uk.q3c.build.gitplus.util.FileBuildPropertiesLoader;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.q3c.build.gitplus.gitplus.GitPlusConfiguration.CloneExistsResponse.EXCEPTION;

/**
 * Created by David Sowerby on 14 Mar 2016
 */
public class GitPlusConfiguration {

    public enum CloneExistsResponse {DELETE, PULL, EXCEPTION}

    public static final Map<String, String> defaultIssueLabels = new ImmutableMap.Builder<String, String>().put("bug", "fc2929")
                                                                                                           .put("duplicate", "cccccc")
                                                                                                           .put("enhancement", "84b6eb")
                                                                                                           .put("question", "cc317c")
                                                                                                           .put("wontfix", "d7e102")
                                                                                                           .put("task", "0b02e1")
                                                                                                           .put("quality", "02d7e1")
                                                                                                           .put("documentation", "eb6420")
                                                                                                           .put("build", "fbca04")
                                                                                                           .put("performance", "d4c5f9")
                                                                                                           .put("critical", "e11d21")
                                                                                                           .build();
    private File projectDir;
    private boolean createLocalRepo;
    private boolean createRemoteRepo;
    private boolean cloneRemoteRepo;
    private String projectName;
    private boolean createProject;
    private File projectDirParent;
    private ProjectCreator projectCreator;
    private GitRemoteFactory gitRemoteFactory;
    private String projectDescription = "";
    private String projectHomePage;
    private boolean publicProject;
    private String confirmRemoteDelete;
    private String remoteRepoHtmlUrl;
    private boolean useWiki = true;
    private ServiceProvider remoteServiceProvider = ServiceProvider.GITHUB;
    private String cloneUrl;
    private BuildPropertiesLoader propertiesLoader;
    private RemoteRepoDeleteApprover repoDeleteApprover;
    private String taggerName;
    private String taggerEmail;
    private String remoteRepoUser;
    private String remoteRepoName;
    private Map<String, String> issueLabels;
    private boolean mergeIssueLabels;
    private CloneExistsResponse cloneExistsResponse = EXCEPTION;
    private FileDeleteApprover fileDeleteApprover;
    private GitLocalProvider gitLocalProvider;
    private ChangeLogConfiguration changeLogConfiguration;

    public GitPlusConfiguration() {
        //required
    }

    public GitPlusConfiguration(@Nonnull GitPlusConfiguration other) {
        checkNotNull(other);
        this.projectDir = other.projectDir;
        this.createLocalRepo = other.createLocalRepo;
        this.createRemoteRepo = other.createRemoteRepo;
        this.cloneRemoteRepo = other.cloneRemoteRepo;
        this.projectName = other.projectName;
        this.createProject = other.createProject;
        this.remoteRepoName = other.remoteRepoName;
        this.remoteRepoUser = other.remoteRepoUser;
        this.projectDirParent = other.projectDirParent;
        this.projectCreator = other.projectCreator;
        this.gitRemoteFactory = other.gitRemoteFactory;
        this.projectDescription = other.projectDescription;
        this.projectHomePage = other.projectHomePage;
        this.publicProject = other.publicProject;
        this.confirmRemoteDelete = other.confirmRemoteDelete;
        this.remoteServiceProvider = other.remoteServiceProvider;
        this.remoteRepoHtmlUrl = other.remoteRepoHtmlUrl;
        this.useWiki = other.useWiki;
        this.propertiesLoader = other.propertiesLoader;
        this.repoDeleteApprover = other.repoDeleteApprover;
        this.cloneUrl = other.cloneUrl;
        this.taggerName = other.taggerName;
        this.taggerEmail = other.taggerEmail;
        this.issueLabels = other.issueLabels;
        this.mergeIssueLabels = other.mergeIssueLabels;
        this.cloneExistsResponse = other.cloneExistsResponse;
        this.fileDeleteApprover = other.fileDeleteApprover;
        this.changeLogConfiguration = other.changeLogConfiguration;
        this.gitLocalProvider = other.gitLocalProvider;

    }

    public GitPlusConfiguration changeLogConfiguration(final ChangeLogConfiguration changeLogConfiguration) {
        this.changeLogConfiguration = changeLogConfiguration;
        return this;
    }

    public ChangeLogConfiguration getChangeLogConfiguration() {
        return changeLogConfiguration;
    }

    public GitPlusConfiguration gitLocalProvider(final GitLocalProvider gitLocalProvider) {
        this.gitLocalProvider = gitLocalProvider;
        return this;
    }

    public GitLocalProvider getGitLocalProvider() {
        if (gitLocalProvider == null) {
            gitLocalProvider = new DefaultGitLocalProvider();
        }
        return gitLocalProvider;
    }

    public boolean isMergeIssueLabels() {
        return mergeIssueLabels;
    }

    public GitPlusConfiguration mergeIssueLabels(final boolean mergeIssueLabels) {
        this.mergeIssueLabels = mergeIssueLabels;
        return this;
    }

    public GitPlusConfiguration issueLabels(final Map<String, String> issueLabels) {
        this.issueLabels = issueLabels;
        return this;
    }

    public Map<String, String> getIssueLabels() {
        if (issueLabels == null) {
            issueLabels = defaultIssueLabels;
        }
        return issueLabels;
    }

    public GitPlusConfiguration remoteRepoName(final String remoteRepoName) {
        this.remoteRepoName = remoteRepoName;
        return this;
    }

    public GitPlusConfiguration remoteRepoUser(final String remoteRepoUser) {
        this.remoteRepoUser = remoteRepoUser;
        return this;
    }

    public String getRemoteRepoUser() {
        return remoteRepoUser;
    }

    public String getRemoteRepoName() {
        return remoteRepoName;
    }

    public String getTaggerName() throws IOException {
        if (taggerName == null) {
            taggerName = getPropertiesLoader().taggerName();
        }
        return taggerName;
    }

    public String getTaggerEmail() throws IOException {
        if (taggerEmail == null) {
            taggerEmail = getPropertiesLoader().taggerEmail();
        }
        return taggerEmail;
    }

    public RemoteRepoDeleteApprover getRepoDeleteApprover() {
        if (repoDeleteApprover == null) {
            repoDeleteApprover = new DefaultRemoteRepoDeleteApprover();
        }
        return repoDeleteApprover;
    }

    public BuildPropertiesLoader getPropertiesLoader() {
        if (propertiesLoader == null) {
            propertiesLoader = new FileBuildPropertiesLoader();
        }
        return propertiesLoader;
    }

    public ServiceProvider getRemoteServiceProvider() {
        return remoteServiceProvider;
    }


    public GitRemoteFactory getGitRemoteFactory() {
        if (gitRemoteFactory == null) {
            gitRemoteFactory = new DefaultGitRemoteFactory();
        }
        return gitRemoteFactory;
    }

    /**
     * Required only if {@link #createProject} is true
     *
     * @param projectCreator implementation of this creates a development project within the {@link #projectDir}
     * @return this for fluency
     */
    public GitPlusConfiguration projectCreator(final ProjectCreator projectCreator) {
        this.projectCreator = projectCreator;
        return this;
    }

    public ProjectCreator getProjectCreator() {
        return projectCreator;
    }

    public GitPlusConfiguration projectDir(final File projectDir) {
        this.projectDir = projectDir;
        return this;
    }

    public String getRemoteRepoHtmlUrl() {
        try {
            if (remoteRepoHtmlUrl == null) {
                remoteRepoHtmlUrl = getGitRemoteFactory().htmlUrlFromRepoName(remoteRepoUser, remoteRepoName);
            }
        } catch (NullPointerException npe) {
            throw new GitPlusConfigurationException("unable to retrieve Url for remote repo", npe);
        }
        return remoteRepoHtmlUrl;
    }


    public File getProjectDir() {
        if (projectDir == null) {
            projectDir = new File(getProjectDirParent(), getProjectName());
        }
        return projectDir;
    }


    public boolean isCreateLocalRepo() {
        return createLocalRepo;
    }


    public boolean isCreateRemoteRepo() {
        return createRemoteRepo;
    }


    public boolean isCloneRemoteRepo() {
        return cloneRemoteRepo;
    }


    public String getProjectName() {
        if (projectName == null) {
            projectName = remoteRepoName;
            if (projectName == null) {
                throw new GitPlusConfigurationException("projectName cannot be null.  It can be set directly or it will use remoteRepoName");
            }
        }
        return projectName;
    }


    public boolean isCreateProject() {
        return createProject;
    }


    public GitPlusConfiguration createLocalRepo(final boolean createLocalRepo) {
        this.createLocalRepo = createLocalRepo;
        if (createLocalRepo) {
            cloneRemoteRepo = false;
        }
        return this;
    }

    public GitPlusConfiguration createRemoteRepo(final boolean createRemoteRepo) {
        this.createRemoteRepo = createRemoteRepo;
        return this;
    }

    public GitPlusConfiguration cloneRemoteRepo(final boolean cloneRemoteRepo) {
        this.cloneRemoteRepo = cloneRemoteRepo;
        if (cloneRemoteRepo) {
            createLocalRepo = false;
        }
        return this;
    }

    public GitPlusConfiguration projectName(final String projectName) {
        this.projectName = projectName;
        return this;
    }

    public GitPlusConfiguration createProject(final boolean createProject) {
        this.createProject = createProject;
        if (projectCreator == null) {
            throw new GitPlusConfigurationException("When 'createProject' is true, 'projectCreator' cannot be null");
        }
        return this;
    }


    public File getProjectDirParent() {
        if (projectDirParent == null) {
            projectDirParent = new File(".");
        }
        return projectDirParent;
    }

    public GitPlusConfiguration projectDirParent(final File projectDirParent) {
        this.projectDirParent = projectDirParent;
        return this;
    }

    public GitPlusConfiguration gitRemoteFactory(final GitRemoteFactory gitRemoteFactory) {
        this.gitRemoteFactory = gitRemoteFactory;
        return this;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public GitPlusConfiguration projectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
        return this;
    }

    public String getProjectHomePage() {
        return projectHomePage;
    }

    public GitPlusConfiguration projectHomePage(String projectHomePage) {
        this.projectHomePage = projectHomePage;
        return this;
    }

    public boolean isPublicProject() {
        return publicProject;
    }

    public GitPlusConfiguration publicProject(boolean publicProject) {
        this.publicProject = publicProject;
        return this;
    }

    public String getConfirmRemoteDelete() {
        return confirmRemoteDelete;
    }

    public GitPlusConfiguration confirmRemoteDelete(String confirmRemoteDelete) {
        this.confirmRemoteDelete = confirmRemoteDelete;
        return this;
    }


    public GitPlusConfiguration remoteServiceProvider(final ServiceProvider remoteServiceProvider) {
        this.remoteServiceProvider = remoteServiceProvider;
        return this;
    }

    public GitPlusConfiguration remoteRepoHtmlUrl(final String remoteRepoHtmlUrl) {
        this.remoteRepoHtmlUrl = remoteRepoHtmlUrl;
        return this;
    }

    public boolean isUseWiki() {
        return useWiki;
    }

    /**
     * Determinses whether wiki is used.  This option has limited value for the GitHub implementation, as that provides no facility for turning off wiki
     * enablement.  Thus for GitHub a wiki is always enabled - however, setting this property to false will prevent the wiki from being cloned or pushed.
     *
     * @param useWiki true to use the wiki
     * @return this for fluency
     */
    public GitPlusConfiguration useWiki(final boolean useWiki) {
        this.useWiki = useWiki;
        return this;
    }


    public String getCloneUrl() {
        if (cloneUrl == null) {
            cloneUrl = getGitRemoteFactory().cloneUrlFromHtmlUrl(getRemoteRepoHtmlUrl());
        }
        return cloneUrl;
    }

    public GitPlusConfiguration cloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
        return this;
    }

    public GitPlusConfiguration propertiesLoader(final BuildPropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
        return this;
    }

    public String getApiTokenRestricted() throws IOException {
        return getPropertiesLoader().apiTokenRestricted(remoteServiceProvider);
    }

    public String getApiTokenCreateRepo() throws IOException {
        return getPropertiesLoader().apiTokenRepoCreate(remoteServiceProvider);
    }

    public String getApiTokenDeleteRepo() throws IOException {
        return getPropertiesLoader().apiTokenRepoDelete(remoteServiceProvider);
    }

    public boolean deleteRepoApproved() {
        return getRepoDeleteApprover().isApproved(this);
    }

    public GitPlusConfiguration repoDeleteApprover(final RemoteRepoDeleteApprover repoDeleteApprover) {
        this.repoDeleteApprover = repoDeleteApprover;
        return this;
    }

    public GitPlusConfiguration taggerEmail(final String taggerEmail) {
        this.taggerEmail = taggerEmail;
        return this;
    }

    public GitPlusConfiguration taggerName(final String taggerName) {
        this.taggerName = taggerName;
        return this;
    }


    public String getRemoteRepoFullName() {
        if (remoteRepoUser == null || remoteRepoName == null) {
            return null;
        }
        return remoteRepoUser + "/" + remoteRepoName;
    }

    public GitPlusConfiguration remoteRepoFullName(String fullRepoName) {
        String[] splitRepoName = fullRepoName.split("/");
        if (splitRepoName.length != 2) {
            throw new GitPlusConfigurationException("Structure of full repo name is invalid, '" + fullRepoName + "', it must be of the form 'user/repo'");
        }
        remoteRepoUser = splitRepoName[0];
        remoteRepoName = splitRepoName[1];
        return this;
    }

    public CloneExistsResponse getCloneExistsResponse() {
        return cloneExistsResponse;
    }

    public GitPlusConfiguration cloneExistsResponse(final CloneExistsResponse cloneExistsResponse) {
        this.cloneExistsResponse = cloneExistsResponse;
        return this;
    }


    public FileDeleteApprover getFileDeleteApprover() {
        return fileDeleteApprover;
    }

    public GitPlusConfiguration fileDeleteApprover(final FileDeleteApprover fileDeleteApprover) {
        this.fileDeleteApprover = fileDeleteApprover;
        return this;
    }


}