package uk.q3c.build.gitplus;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import uk.q3c.build.gitplus.gitplus.DefaultGitPlus;
import uk.q3c.build.gitplus.gitplus.DefaultGitPlusConfiguration;
import uk.q3c.build.gitplus.gitplus.DefaultUrlParser;
import uk.q3c.build.gitplus.gitplus.GitPlus;
import uk.q3c.build.gitplus.gitplus.GitPlusConfiguration;
import uk.q3c.build.gitplus.gitplus.UrlParser;
import uk.q3c.build.gitplus.local.BranchConfigProvider;
import uk.q3c.build.gitplus.local.DefaultBranchConfigProvider;
import uk.q3c.build.gitplus.local.DefaultGitCloner;
import uk.q3c.build.gitplus.local.DefaultGitInitChecker;
import uk.q3c.build.gitplus.local.DefaultGitLocal;
import uk.q3c.build.gitplus.local.DefaultGitLocalConfiguration;
import uk.q3c.build.gitplus.local.DefaultGitProvider;
import uk.q3c.build.gitplus.local.DefaultWikiLocal;
import uk.q3c.build.gitplus.local.GitCloner;
import uk.q3c.build.gitplus.local.GitInitChecker;
import uk.q3c.build.gitplus.local.GitLocal;
import uk.q3c.build.gitplus.local.GitLocalConfiguration;
import uk.q3c.build.gitplus.local.GitProvider;
import uk.q3c.build.gitplus.local.WikiLocal;
import uk.q3c.build.gitplus.remote.DefaultGitRemoteConfiguration;
import uk.q3c.build.gitplus.remote.DefaultGitRemoteResolver;
import uk.q3c.build.gitplus.remote.DefaultRemoteRequest;
import uk.q3c.build.gitplus.remote.GitRemote;
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration;
import uk.q3c.build.gitplus.remote.GitRemoteResolver;
import uk.q3c.build.gitplus.remote.RemoteRequest;
import uk.q3c.build.gitplus.remote.ServiceProvider;
import uk.q3c.build.gitplus.remote.bitbucket.BitBucketProvider;
import uk.q3c.build.gitplus.remote.bitbucket.BitBucketRemote;
import uk.q3c.build.gitplus.remote.bitbucket.DefaultBitBucketProvider;
import uk.q3c.build.gitplus.remote.bitbucket.DefaultBitBucketRemote;
import uk.q3c.build.gitplus.remote.github.DefaultGitHubProvider;
import uk.q3c.build.gitplus.remote.github.DefaultGitHubRemote;
import uk.q3c.build.gitplus.remote.github.GitHubProvider;
import uk.q3c.build.gitplus.remote.github.GitHubRemote;
import uk.q3c.build.gitplus.util.DefaultPropertiesResolver;
import uk.q3c.build.gitplus.util.FilePropertiesLoader;
import uk.q3c.build.gitplus.util.PropertiesLoader;
import uk.q3c.build.gitplus.util.PropertiesResolver;

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
        bind(GitRemoteResolver.class).to(DefaultGitRemoteResolver.class);
        bind(BitBucketRemote.class).to(DefaultBitBucketRemote.class);
        bind(PropertiesLoader.class).to(FilePropertiesLoader.class);
        bind(BitBucketProvider.class).to(DefaultBitBucketProvider.class);
        bind(GitProvider.class).to(DefaultGitProvider.class);
        bind(BranchConfigProvider.class).to(DefaultBranchConfigProvider.class);
        bind(GitInitChecker.class).to(DefaultGitInitChecker.class);
        bind(GitCloner.class).to(DefaultGitCloner.class);
        bind(PropertiesResolver.class).to(DefaultPropertiesResolver.class);
        bind(GitPlusConfiguration.class).to(DefaultGitPlusConfiguration.class);
        bind(UrlParser.class).to(DefaultUrlParser.class);

    }
}
