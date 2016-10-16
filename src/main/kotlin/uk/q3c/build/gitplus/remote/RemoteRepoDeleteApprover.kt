package uk.q3c.build.gitplus.remote

/**
 * Created by David Sowerby on 05 Apr 2016
 */
interface RemoteRepoDeleteApprover {
    fun isApproved(configuration: GitRemoteConfiguration): Boolean
}
