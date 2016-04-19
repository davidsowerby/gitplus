package uk.q3c.build.gitplus.local;

import uk.q3c.build.gitplus.gitplus.GitPlusConfiguration;

import javax.annotation.Nonnull;

/**
 * Created by David Sowerby on 17 Apr 2016
 */
public class DefaultGitLocalProvider implements GitLocalProvider {
    @Override
    public GitLocal get(@Nonnull GitPlusConfiguration gitPlusConfiguration) {
        return new GitLocal(gitPlusConfiguration);
    }
}
