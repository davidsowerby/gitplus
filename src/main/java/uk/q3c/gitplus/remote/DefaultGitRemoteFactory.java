package uk.q3c.gitplus.remote;

import uk.q3c.gitplus.gitplus.GitPlusConfiguration;
import uk.q3c.gitplus.gitplus.GitPlusConfigurationException;
import uk.q3c.gitplus.remote.GitRemote.ServiceProvider;

import javax.annotation.Nonnull;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class would need to be replaced if there were more than one remote source (for example BitBucket)
 * <p>
 * Created by David Sowerby on 16 Mar 2016
 */
public class DefaultGitRemoteFactory implements GitRemoteFactory {

    private ServiceProvider remoteServiceProvider;

    @Override
    public ServiceProvider getRemoteServiceProvider() {
        return remoteServiceProvider;
    }

    @Override
    public void setRemoteServiceProvider(@Nonnull ServiceProvider remoteServiceProvider) {
        this.remoteServiceProvider = remoteServiceProvider;
    }

    @Override
    public GitRemote createRemoteInstance(@Nonnull GitPlusConfiguration gitPlusConfiguration) throws IOException {
        checkNotNull(gitPlusConfiguration);
        return new GitHubRemote(gitPlusConfiguration, new GitHubProvider(), new RemoteRequest());
    }

    @Override
    public String htmlUrlStem() {
        return "https://github.com";
    }

    @Override
    public String htmlUrlFromFullRepoName(@Nonnull String fullRepoName) {
        checkNotNull(fullRepoName);
        return htmlUrlStem() + '/' + fullRepoName;
    }

    @Override
    public String htmlTagUrlFromFullRepoName(@Nonnull String fullRepoName) {
        checkNotNull(fullRepoName);
        return htmlUrlFromFullRepoName(fullRepoName) + "/tree";
    }

    @Override
    public String apiUrlStem() {
        return "https://api.github.com";
    }

    @Override
    public String cloneUrlFromFullRepoName(@Nonnull String fullRepoName) {
        checkNotNull(fullRepoName);
        return htmlUrlStem() + '/' + fullRepoName + ".git";
    }

    @Override
    public String fullRepoNameFromHtmlUrl(@Nonnull String htmlUrl) {
        checkNotNull(htmlUrl);
        return htmlUrl.replaceFirst(htmlUrlStem() + '/', "");
    }

    @Override
    public String projectNameFromFullRepoName(@Nonnull String remoteRepoFullName) {
        if (!remoteRepoFullName.contains("/")) {
            throw new GitPlusConfigurationException("Repo full name must be of the form 'owner/repo' ");
        }
        return remoteRepoFullName.split("/")[1];
    }

    @Override
    public String fullRepoNameFromCloneUrl(@Nonnull String cloneUrl) {
        checkNotNull(cloneUrl);
        String htmlUrl = htmlUrlFromCloneUrl(cloneUrl);
        return fullRepoNameFromHtmlUrl(htmlUrl);

    }

    @Override
    public String htmlUrlFromCloneUrl(@Nonnull String cloneUrl) {
        checkNotNull(cloneUrl);
        return cloneUrl.substring(0, cloneUrl.length() - 4);

    }

    @Override
    public String wikiHtmlUrlFromCoreHtmlUrl(@Nonnull String coreHtmlUrl) {
        checkNotNull(coreHtmlUrl);
        return coreHtmlUrl + "/wiki";
    }

    @Override
    public String wikiCloneUrlFromCoreHtmLUrl(@Nonnull String coreHtmlUrl) {
        checkNotNull(coreHtmlUrl);
        return coreHtmlUrl + (".wiki.git");
    }

    @Override
    public String cloneUrlFromHtmlUrl(@Nonnull String remoteRepoHtmlUrl) {
        return remoteRepoHtmlUrl + ".git";
    }
}
