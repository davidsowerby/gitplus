package uk.q3c.build.gitplus.remote

/**
 * Created by David Sowerby on 05 Apr 2016
 */
class DefaultRemoteRepoDeleteApprover : RemoteRepoDeleteApprover {
    override fun isApproved(configuration: GitRemoteConfiguration): Boolean {
        val confirmationMessage = "I really, really want to delete the " + configuration.remoteRepoFullName() + " repo from GitHub"
        return confirmationMessage == configuration.confirmDelete
    }
}
