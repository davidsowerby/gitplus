package uk.q3c.gitplus.remote;

import uk.q3c.gitplus.gitplus.GitPlusConfiguration;

/**
 * Created by David Sowerby on 05 Apr 2016
 */
public interface RemoteRepoDeleteApprover {
    boolean isApproved(GitPlusConfiguration gitPlusConfiguration);
}
