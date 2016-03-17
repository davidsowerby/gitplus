package uk.q3c.gitplus.remote;

import uk.q3c.gitplus.gitplus.GitPlusConfiguration;
import uk.q3c.gitplus.gitplus.GitPlusConfigurationException;
import uk.q3c.gitplus.remote.GitRemote.Provider;

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
    public GitRemote create(@Nonnull GitPlusConfiguration gitPlusConfiguration) throws IOException {
        checkNotNull(gitPlusConfiguration);
        return new GitHubRemote(gitPlusConfiguration, new GitHubProvider(), new RemoteRequest());
    }

    @Override
    public String htmlUrlStem(@Nonnull Provider provider) {
        checkNotNull(provider);
        return "https://github.com";
    }

    @Override
    public String htmlUrlFromFullRepoName(@Nonnull Provider provider, @Nonnull String fullRepoName) {
        checkNotNull(provider);
        checkNotNull(fullRepoName);
        return htmlUrlStem(provider) + '/' + fullRepoName;
    }

    @Override
    public String htmlTagUrl(@Nonnull Provider provider, @Nonnull String fullRepoName) {
        checkNotNull(provider);
        checkNotNull(fullRepoName);
        return htmlUrlFromFullRepoName(provider, fullRepoName) + "/tree";
    }

    @Override
    public String apiUrlStem(@Nonnull Provider provider) {
        checkNotNull(provider);
        return "https://api.github.com";
    }

    @Override
    public String cloneUrl(@Nonnull Provider provider, @Nonnull String fullRepoName) {
        checkNotNull(provider);
        checkNotNull(fullRepoName);
        return htmlUrlStem(provider) + '/' + fullRepoName + ".git";
    }

    @Override
    public String repoFullNameFromHtmlUrl(@Nonnull Provider provider, @Nonnull String htmlUrl) {
        checkNotNull(provider);
        checkNotNull(htmlUrl);
        return htmlUrl.replaceFirst(htmlUrlStem(provider) + '/', "");
    }

    @Override
    public String projectNameFromRemoteRepFullName(@Nonnull Provider provider, @Nonnull String remoteRepoFullName) {
        if (!remoteRepoFullName.contains("/")) {
            throw new GitPlusConfigurationException("Repo full name must be of the form 'owner/repo' ");
        }
        return remoteRepoFullName.split("/")[1];
    }

    @Override
    public String repoFullNameFromCloneUrl(@Nonnull Provider provider, @Nonnull String cloneUrl) {
        checkNotNull(provider);
        checkNotNull(cloneUrl);
        String htmlUrl = htmlUrlFromCloneUrl(provider, cloneUrl);
        return repoFullNameFromHtmlUrl(provider, htmlUrl);

    }

    @Override
    public String htmlUrlFromCloneUrl(@Nonnull Provider provider, @Nonnull String cloneUrl) {
        checkNotNull(provider);
        checkNotNull(cloneUrl);
        return cloneUrl.substring(cloneUrl.length() - 4);

    }
}
