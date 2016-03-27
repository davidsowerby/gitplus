package uk.q3c.gitplus.remote;

import uk.q3c.gitplus.gitplus.GitPlusConfiguration;
import uk.q3c.gitplus.remote.GitRemote.ServiceProvider;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by David Sowerby on 16 Mar 2016
 */
public interface GitRemoteFactory {

    /**
     * Creates an instance of a {@link GitRemote} implementation from the information provided in {@code gitPlusConfiguration}
     *
     * @param gitPlusConfiguration the configuration containing information
     * @return an instance of a {@link GitRemote} implementation from the information provided in {@code gitPlusConfiguration}
     */
    GitRemote createRemoteInstance(@Nonnull GitPlusConfiguration gitPlusConfiguration) throws IOException;

    String htmlUrlStem(@Nonnull ServiceProvider serviceProvider);

    String htmlUrlFromFullRepoName(@Nonnull ServiceProvider serviceProvider, @Nonnull String fullRepoName);

    String htmlTagUrl(@Nonnull ServiceProvider serviceProvider, @Nonnull String fullRepoName);

    String apiUrlStem(@Nonnull ServiceProvider serviceProvider);

    String cloneUrl(@Nonnull ServiceProvider serviceProvider, @Nonnull String fullRepoName);

    String repoFullNameFromHtmlUrl(@Nonnull ServiceProvider serviceProvider, @Nonnull String htmlUrl);

    String projectNameFromRemoteRepFullName(@Nonnull ServiceProvider remoteServiceProvider, @Nonnull String remoteRepoFullName);

    String repoFullNameFromCloneUrl(@Nonnull ServiceProvider remoteServiceProvider, @Nonnull String origin);

    String htmlUrlFromCloneUrl(@Nonnull ServiceProvider serviceProvider, @Nonnull String cloneUrl);
}
