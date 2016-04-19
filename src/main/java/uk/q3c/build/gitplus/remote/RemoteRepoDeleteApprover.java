package uk.q3c.build.gitplus.remote;

import uk.q3c.build.gitplus.gitplus.GitPlusConfiguration;

/**
 * Created by David Sowerby on 05 Apr 2016
 */
@FunctionalInterface
public interface RemoteRepoDeleteApprover {
    boolean isApproved(GitPlusConfiguration gitPlusConfiguration);
}
