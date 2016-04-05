package uk.q3c.gitplus.gitplus;

import uk.q3c.gitplus.remote.DefaultGitRemoteFactory;
import uk.q3c.gitplus.remote.DefaultRemoteRepoDeleteApprover;
import uk.q3c.gitplus.remote.GitRemote.ServiceProvider;
import uk.q3c.gitplus.remote.GitRemoteFactory;
import uk.q3c.gitplus.remote.RemoteRepoDeleteApprover;
import uk.q3c.gitplus.util.BuildPropertiesLoader;
import uk.q3c.gitplus.util.FileBuildPropertiesLoader;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by David Sowerby on 14 Mar 2016
 */
public class GitPlusConfiguration {

    private File projectDir;
    private boolean createLocalRepo;
    private boolean createRemoteRepo;
    private boolean cloneRemoteRepo;
    private String projectName;
    private boolean createProject;
    private String remoteRepoFullName;
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
        this.remoteRepoFullName = other.remoteRepoFullName;
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


    public String getRemoteRepoFullName() {
        return remoteRepoFullName;
    }

    public GitPlusConfiguration remoteRepoFullName(final String remoteRepoFullName) {
        this.remoteRepoFullName = remoteRepoFullName;
        return this;
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
        if (gitRemoteFactory == null) {
            throw new GitPlusConfigurationException("gitRemoteFactory has not been set, have you forgotten to call validate()?");
        }
        if (remoteRepoHtmlUrl == null) {
            remoteRepoHtmlUrl = gitRemoteFactory.htmlUrlFromFullRepoName(remoteRepoFullName);
        }
        return remoteRepoHtmlUrl;
    }


    public File getProjectDir() {
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
            if (remoteRepoFullName != null) {
                projectName = remoteRepoFullName.split("/")[1];
            }
        }
        return projectName;
    }


    public boolean isCreateProject() {
        return createProject;
    }


    public void validate() {

        getGitRemoteFactory().setRemoteServiceProvider(getRemoteServiceProvider());

        if (createProject) {
            exceptionIfNull("createProject", "projectCreator", projectCreator);
        }

        prepareRemoteConfig();
        checkProjectDir();

        if (createLocalRepo) {
            exceptionIfNull("createLocalRepo", "projectName", projectName);
        }
    }

    private void prepareRemoteConfig() {
        if (createRemoteRepo) {
            exceptionIfNull("createRemoteRepo", "projectDescription", projectDescription);
        }
        if (cloneRemoteRepo) {
            if (projectDirParent == null) {
                projectDirParent = new File(".");
            }
        }

        if (createRemoteRepo || cloneRemoteRepo) {
            exceptionIfNull("createRemoteRepo OR cloneRemoteRepo", "remoteRepoFullName", remoteRepoFullName);
            remoteRepoHtmlUrl = gitRemoteFactory.htmlUrlFromFullRepoName(remoteRepoFullName);
        }
    }


    private void checkProjectDir() {
        if (projectDir == null) {
            if (projectDirParent == null) {
                projectDirParent = new File(".");
            }
            if (projectName == null) {
                if (remoteRepoFullName != null) {
                    projectName = gitRemoteFactory.projectNameFromFullRepoName(remoteRepoFullName);
                } else {
                    throw new GitPlusConfigurationException("If projectDir is null, projectName cannot be null");
                }
            }
            projectDir = new File(projectDirParent, projectName);
        }
    }


    private void exceptionIfNull(String flagName, String propertyName, Object propertyValue) {
        if (propertyValue == null) {
            throw new GitPlusConfigurationException("When " + flagName + " is true, " + propertyName + " is required");
        }
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
        return this;
    }


    public File getProjectDirParent() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GitPlusConfiguration that = (GitPlusConfiguration) o;

        if (createLocalRepo != that.createLocalRepo) {
            return false;
        }
        if (createRemoteRepo != that.createRemoteRepo) {
            return false;
        }
        if (cloneRemoteRepo != that.cloneRemoteRepo) {
            return false;
        }
        if (createProject != that.createProject) {
            return false;
        }
        if (publicProject != that.publicProject) {
            return false;
        }
        if (useWiki != that.useWiki) {
            return false;
        }
        if (projectDir != null ? !projectDir.equals(that.projectDir) : that.projectDir != null) {
            return false;
        }
        if (projectName != null ? !projectName.equals(that.projectName) : that.projectName != null) {
            return false;
        }

        if (taggerName != null ? !taggerName.equals(that.taggerName) : that.taggerName != null) {
            return false;
        }

        if (taggerEmail != null ? !taggerEmail.equals(that.taggerEmail) : that.taggerEmail != null) {
            return false;
        }

        if (remoteRepoFullName != null ? !remoteRepoFullName.equals(that.remoteRepoFullName) : that.remoteRepoFullName != null) {
            return false;
        }
        if (projectDirParent != null ? !projectDirParent.equals(that.projectDirParent) : that.projectDirParent != null) {
            return false;
        }
        if (projectCreator != null ? !projectCreator.equals(that.projectCreator) : that.projectCreator != null) {
            return false;
        }
        if (gitRemoteFactory != null ? !gitRemoteFactory.equals(that.gitRemoteFactory) : that.gitRemoteFactory != null) {
            return false;
        }
        if (projectDescription != null ? !projectDescription.equals(that.projectDescription) : that.projectDescription != null) {
            return false;
        }
        if (projectHomePage != null ? !projectHomePage.equals(that.projectHomePage) : that.projectHomePage != null) {
            return false;
        }
        if (confirmRemoteDelete != null ? !confirmRemoteDelete.equals(that.confirmRemoteDelete) : that.confirmRemoteDelete != null) {
            return false;
        }
        if (remoteRepoHtmlUrl != null ? !remoteRepoHtmlUrl.equals(that.remoteRepoHtmlUrl) : that.remoteRepoHtmlUrl != null) {
            return false;
        }
        return remoteServiceProvider == that.remoteServiceProvider;

    }

    @Override
    public int hashCode() {
        int result = projectDir != null ? projectDir.hashCode() : 0;
        result = 31 * result + (createLocalRepo ? 1 : 0);
        result = 31 * result + (createRemoteRepo ? 1 : 0);
        result = 31 * result + (cloneRemoteRepo ? 1 : 0);
        result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
        result = 31 * result + (taggerName != null ? taggerName.hashCode() : 0);
        result = 31 * result + (taggerEmail != null ? taggerEmail.hashCode() : 0);
        result = 31 * result + (createProject ? 1 : 0);
        result = 31 * result + (remoteRepoFullName != null ? remoteRepoFullName.hashCode() : 0);
        result = 31 * result + (projectDirParent != null ? projectDirParent.hashCode() : 0);
        result = 31 * result + (projectCreator != null ? projectCreator.hashCode() : 0);
        result = 31 * result + (gitRemoteFactory != null ? gitRemoteFactory.hashCode() : 0);
        result = 31 * result + (projectDescription != null ? projectDescription.hashCode() : 0);
        result = 31 * result + (projectHomePage != null ? projectHomePage.hashCode() : 0);
        result = 31 * result + (publicProject ? 1 : 0);
        result = 31 * result + (confirmRemoteDelete != null ? confirmRemoteDelete.hashCode() : 0);
        result = 31 * result + (remoteRepoHtmlUrl != null ? remoteRepoHtmlUrl.hashCode() : 0);
        result = 31 * result + (useWiki ? 1 : 0);
        result = 31 * result + (remoteServiceProvider != null ? remoteServiceProvider.hashCode() : 0);
        return result;
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
}