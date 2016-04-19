package uk.q3c.build.gitplus.remote;

import uk.q3c.build.gitplus.gitplus.GitPlusConfiguration;
import uk.q3c.build.gitplus.remote.GitRemote.ServiceProvider;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by David Sowerby on 16 Mar 2016
 */
public interface GitRemoteFactory {

    ServiceProvider getRemoteServiceProvider();

    void setRemoteServiceProvider(@Nonnull ServiceProvider remoteServiceProvider);

    /**
     * Creates an instance of a {@link GitRemote} implementation from the information provided in {@code gitPlusConfiguration}
     *
     * @param gitPlusConfiguration the configuration containing information
     * @return an instance of a {@link GitRemote} implementation from the information provided in {@code gitPlusConfiguration}
     */
    GitRemote createRemoteInstance(@Nonnull GitPlusConfiguration gitPlusConfiguration) throws IOException;

    String htmlUrlStem();

    String htmlUrlFromRepoName(@Nonnull String remoteRepoUser, @Nonnull String remoteRepoName);


    String htmlTagUrlFromFullRepoName(@Nonnull String remoteRepoUser, @Nonnull String remoteRepoName);

    String apiUrlStem();


    String fullRepoNameFromHtmlUrl(@Nonnull String htmlUrl);


    String fullRepoNameFromCloneUrl(@Nonnull String origin);

    String htmlUrlFromCloneUrl(@Nonnull String cloneUrl);

    String wikiHtmlUrlFromCoreHtmlUrl(@Nonnull String coreHtmlUrl);

    String wikiCloneUrlFromCoreHtmLUrl(@Nonnull String coreCloneUrl);

    String cloneUrlFromHtmlUrl(@Nonnull String remoteRepoHtmlUrl);
}
