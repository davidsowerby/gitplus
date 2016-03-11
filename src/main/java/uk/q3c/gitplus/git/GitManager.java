package uk.q3c.gitplus.git;


import uk.q3c.gitplus.origin.GitHubServiceApi;
import uk.q3c.gitplus.origin.OriginServiceApi;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages the combination of local Git repo with its origin, which is taken to be a hosted online service such as GitHub, which is also assumed to provide
 * issue tracking
 * <p>
 * Created by David Sowerby on 08 Mar 2016
 */
public class GitManager {

    private final GitHandler gitHandler;
    private final ChangeLog changeLog;
    private OriginServiceApi originServiceApi;
    private OriginServiceApi.Provider originServiceProvider = OriginServiceApi.Provider.GITHUB;
    private File projectDir;
    private String apiToken;
    private String repoName;

    public GitManager() {
        constructOriginHandler();
        gitHandler = new GitHandler();
        changeLog = new ChangeLog();
        changeLog.setGitHandler(gitHandler);
        changeLog.setOriginServiceApi(originServiceApi);
    }

    public String getRepoName() {
        return repoName;
    }

    /**
     * Set the repo name of a structure correct for the origin service provider (so for GitHub it would be something like "davidsowerby/krail")
     *
     * @param repoName the repo name of a structure correct for the origin service provider
     */
    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public ChangeLog getChangeLog() {
        return changeLog;
    }

    public GitHandler getGitHandler() {

        return gitHandler;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(@Nonnull String apiToken) {
        checkNotNull(apiToken);
        this.apiToken = apiToken;
    }

    public OriginServiceApi getOriginServiceApi() {
        return originServiceApi;
    }


    public OriginServiceApi.Provider getOriginServiceProvider() {
        return originServiceProvider;
    }

    public void setOriginServiceProvider(OriginServiceApi.Provider originServiceProvider) {
        this.originServiceProvider = originServiceProvider;
        constructOriginHandler();
    }

    private void constructOriginHandler() {
        switch (originServiceProvider) {
            case GITHUB:
                originServiceApi = new GitHubServiceApi();
                originServiceApi.setApiToken(apiToken);
                originServiceApi.setRepoName(repoName);
                break;
            default:
                originServiceApi = new GitHubServiceApi();
                originServiceApi.setApiToken(apiToken);
        }
        originServiceApi.setRepoName(gitHandler.getOriginRepoBaseUrl());
    }

    public File getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(@Nonnull File projectDir) {
        checkNotNull(projectDir);
        this.projectDir = projectDir;
        gitHandler.setProjectDir(projectDir);
    }

    public void generateChangeLog() throws IOException {
        changeLog.createChangeLog();
    }
}
