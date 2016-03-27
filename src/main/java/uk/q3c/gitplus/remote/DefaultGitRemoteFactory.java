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

    @Override
    public GitRemote createRemoteInstance(@Nonnull GitPlusConfiguration gitPlusConfiguration) throws IOException {
        checkNotNull(gitPlusConfiguration);
        return new GitHubRemote(gitPlusConfiguration, new GitHubProvider(), new RemoteRequest());
    }

    @Override
    public String htmlUrlStem(@Nonnull ServiceProvider serviceProvider) {
        checkNotNull(serviceProvider);
        return "https://github.com";
    }

    @Override
    public String htmlUrlFromFullRepoName(@Nonnull ServiceProvider serviceProvider, @Nonnull String fullRepoName) {
        checkNotNull(serviceProvider);
        checkNotNull(fullRepoName);
        return htmlUrlStem(serviceProvider) + '/' + fullRepoName;
    }

    @Override
    public String htmlTagUrl(@Nonnull ServiceProvider serviceProvider, @Nonnull String fullRepoName) {
        checkNotNull(serviceProvider);
        checkNotNull(fullRepoName);
        return htmlUrlFromFullRepoName(serviceProvider, fullRepoName) + "/tree";
    }

    @Override
    public String apiUrlStem(@Nonnull ServiceProvider serviceProvider) {
        checkNotNull(serviceProvider);
        return "https://api.github.com";
    }

    @Override
    public String cloneUrl(@Nonnull ServiceProvider serviceProvider, @Nonnull String fullRepoName) {
        checkNotNull(serviceProvider);
        checkNotNull(fullRepoName);
        return htmlUrlStem(serviceProvider) + '/' + fullRepoName + ".git";
    }

    @Override
    public String repoFullNameFromHtmlUrl(@Nonnull ServiceProvider serviceProvider, @Nonnull String htmlUrl) {
        checkNotNull(serviceProvider);
        checkNotNull(htmlUrl);
        return htmlUrl.replaceFirst(htmlUrlStem(serviceProvider) + '/', "");
    }

    @Override
    public String projectNameFromRemoteRepFullName(@Nonnull ServiceProvider serviceProvider, @Nonnull String remoteRepoFullName) {
        if (!remoteRepoFullName.contains("/")) {
            throw new GitPlusConfigurationException("Repo full name must be of the form 'owner/repo' ");
        }
        return remoteRepoFullName.split("/")[1];
    }

    @Override
    public String repoFullNameFromCloneUrl(@Nonnull ServiceProvider serviceProvider, @Nonnull String cloneUrl) {
        checkNotNull(serviceProvider);
        checkNotNull(cloneUrl);
        String htmlUrl = htmlUrlFromCloneUrl(serviceProvider, cloneUrl);
        return repoFullNameFromHtmlUrl(serviceProvider, htmlUrl);

    }

    @Override
    public String htmlUrlFromCloneUrl(@Nonnull ServiceProvider serviceProvider, @Nonnull String cloneUrl) {
        checkNotNull(serviceProvider);
        checkNotNull(cloneUrl);
        return cloneUrl.substring(cloneUrl.length() - 4);

    }
}
