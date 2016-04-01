package uk.q3c.gitplus.local;

import uk.q3c.gitplus.gitplus.GitPlusConfiguration;

import javax.annotation.Nonnull;

/**
 * Created by David Sowerby on 01 Apr 2016
 */
public class GitLocalProvider {

    public GitLocal get(@Nonnull GitPlusConfiguration gitPlusConfiguration) {
        return new GitLocal(gitPlusConfiguration);
    }
}
