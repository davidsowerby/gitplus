package uk.q3c.gitplus.gitplus;

import uk.q3c.gitplus.remote.DefaultGitRemoteFactory;
import uk.q3c.gitplus.remote.GitRemote.Provider;
import uk.q3c.gitplus.remote.GitRemoteFactory;

import java.io.File;

/**
 * Created by David Sowerby on 14 Mar 2016
 */
public class GitPlusConfiguration {

    private File projectDir;
    private String apiToken;
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
    private boolean localOnly;
    private Provider remoteProvider = Provider.GITHUB;

    public Provider getRemoteProvider() {
        return remoteProvider;
    }

    public GitPlusConfiguration localOnly(final boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }

    public boolean isLocalOnly() {
        return localOnly;
    }

    public String getRemoteRepoFullName() {
        return remoteRepoFullName;
    }

    public GitPlusConfiguration remoteRepoFullName(final String remoteRepoFullName) {
        this.remoteRepoFullName = remoteRepoFullName;
        return this;
    }

    public GitRemoteFactory getGitRemoteFactory() {
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

    public String getRemoteRepoUrl() {
        if (gitRemoteFactory == null) {
            throw new GitPlusConfigurationException("gitRemoteFactory has not been set.  Call validate() before calling this method");
        }
        return gitRemoteFactory.htmlUrlFromFullRepoName(remoteProvider, remoteRepoFullName);
    }


    public File getProjectDir() {
        return projectDir;
    }


    public String getApiToken() {
        return apiToken;
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

        if (gitRemoteFactory == null) {
            gitRemoteFactory = new DefaultGitRemoteFactory();
        }

        if (createRemoteRepo) {
            exceptionIfNull("createRemoteRepo", "projectDescription", projectDescription);
        }
        if (cloneRemoteRepo) {
            if (projectDirParent == null) {
                projectDirParent = new File(".");
            }
        }

        if (createProject) {
            exceptionIfNull("createProject", "projectCreator", projectCreator);
        }

        if (createRemoteRepo || cloneRemoteRepo) {
            exceptionIfNull("createRemoteRepo OR cloneRemoteRepo", "apiToken", apiToken);
            exceptionIfNull("createRemoteRepo OR cloneRemoteRepo", "remoteRepoFullName", remoteRepoFullName);
        }


        checkProjectDir();

        if (createLocalRepo) {
            exceptionIfNull("createLocalRepo", "projectName", projectName);
        }
    }


    private void checkProjectDir() {
        if (projectDir == null) {
            if (projectDirParent == null) {
                projectDirParent = new File(".");
            }
            if (projectName == null) {
                if (remoteRepoFullName != null) {
                    projectName = gitRemoteFactory.projectNameFromRemoteRepFullName(remoteProvider, remoteRepoFullName);
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


    public GitPlusConfiguration apiToken(final String apiToken) {
        this.apiToken = apiToken;
        return this;
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


    public GitPlusConfiguration remoteProvider(final Provider remoteProvider) {
        this.remoteProvider = remoteProvider;
        return this;
    }
}