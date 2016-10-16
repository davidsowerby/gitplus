package uk.q3c.build.gitplus;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import uk.q3c.build.gitplus.gitplus.DefaultGitPlus;
import uk.q3c.build.gitplus.gitplus.GitPlus;
import uk.q3c.build.gitplus.local.*;
import uk.q3c.build.gitplus.remote.*;
import uk.q3c.build.gitplus.remote.bitbucket.BitBucketProvider;
import uk.q3c.build.gitplus.remote.bitbucket.BitBucketRemote;
import uk.q3c.build.gitplus.remote.bitbucket.DefaultBitBucketProvider;
import uk.q3c.build.gitplus.remote.bitbucket.DefaultBitBucketRemote;
import uk.q3c.build.gitplus.remote.github.DefaultGitHubProvider;
import uk.q3c.build.gitplus.remote.github.DefaultGitHubRemote;
import uk.q3c.build.gitplus.remote.github.GitHubProvider;
import uk.q3c.build.gitplus.remote.github.GitHubRemote;
import uk.q3c.build.gitplus.util.BuildPropertiesLoader;
import uk.q3c.build.gitplus.util.FileBuildPropertiesLoader;

/**
 * Created by David Sowerby on 17 Oct 2016
 */
public class GitPlusModule extends AbstractModule {


    @Override
    protected void configure() {
        bind(GitHubRemote.class).to(DefaultGitHubRemote.class);
        MapBinder<ServiceProvider, GitRemote> mapbinder
                = MapBinder.newMapBinder(binder(), ServiceProvider.class, GitRemote.class);
        mapbinder.addBinding(ServiceProvider.GITHUB).to(GitHubRemote.class);
        bind(GitPlus.class).to(DefaultGitPlus.class);
        bind(GitHubProvider.class).to(DefaultGitHubProvider.class);
        bind(RemoteRequest.class).to(DefaultRemoteRequest.class);
        bind(GitLocal.class).to(DefaultGitLocal.class);
        bind(GitLocalConfiguration.class).to(DefaultGitLocalConfiguration.class);
        bind(WikiLocal.class).to(DefaultWikiLocal.class);
        bind(GitRemoteConfiguration.class).to(DefaultGitRemoteConfiguration.class);
        bind(GitRemoteProvider.class).to(DefaultGitRemoteProvider.class);
        bind(BitBucketRemote.class).to(DefaultBitBucketRemote.class);
        bind(BuildPropertiesLoader.class).to(FileBuildPropertiesLoader.class);
        bind(BitBucketProvider.class).to(DefaultBitBucketProvider.class);
        bind(GitProvider.class).to(DefaultGitProvider.class);
        bind(BranchConfigProvider.class).to(DefaultBranchConfigProvider.class);
    }
}
