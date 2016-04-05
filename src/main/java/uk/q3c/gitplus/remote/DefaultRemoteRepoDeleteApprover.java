package uk.q3c.gitplus.remote;

import uk.q3c.gitplus.gitplus.GitPlusConfiguration;

/**
 * Created by David Sowerby on 05 Apr 2016
 */
public class DefaultRemoteRepoDeleteApprover implements RemoteRepoDeleteApprover {
    @Override
    public boolean isApproved(GitPlusConfiguration gitPlusConfiguration) {
        String confirmationMessage = "I really, really want to delete the " + gitPlusConfiguration.getRemoteRepoFullName() + " repo from GitHub";
        return confirmationMessage.equals(gitPlusConfiguration.getConfirmRemoteDelete());
    }
}
