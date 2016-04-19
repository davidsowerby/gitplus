package uk.q3c.build.gitplus.local;

import uk.q3c.build.gitplus.gitplus.GitPlusConfiguration;

import javax.annotation.Nonnull;

/**
 * Created by David Sowerby on 01 Apr 2016
 */
public interface GitLocalProvider {

    GitLocal get(@Nonnull GitPlusConfiguration gitPlusConfiguration);
}
