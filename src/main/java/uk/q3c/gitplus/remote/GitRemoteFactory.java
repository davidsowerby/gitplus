package uk.q3c.gitplus.remote;

import uk.q3c.gitplus.gitplus.GitPlusConfiguration;
import uk.q3c.gitplus.remote.GitRemote.Provider;

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
    GitRemote create(@Nonnull GitPlusConfiguration gitPlusConfiguration) throws IOException;

    String htmlUrlStem(@Nonnull Provider provider);

    String htmlUrlFromFullRepoName(@Nonnull Provider provider, @Nonnull String fullRepoName);

    String htmlTagUrl(@Nonnull Provider provider, @Nonnull String fullRepoName);

    String apiUrlStem(@Nonnull Provider provider);

    String cloneUrl(@Nonnull Provider provider, @Nonnull String fullRepoName);

    String repoFullNameFromHtmlUrl(@Nonnull Provider provider, @Nonnull String htmlUrl);

    String projectNameFromRemoteRepFullName(@Nonnull Provider remoteProvider, @Nonnull String remoteRepoFullName);

    String repoFullNameFromCloneUrl(@Nonnull Provider remoteProvider, @Nonnull String origin);

    String htmlUrlFromCloneUrl(@Nonnull Provider provider, @Nonnull String cloneUrl);
}
