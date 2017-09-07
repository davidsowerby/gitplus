package uk.q3c.build.gitplus.remote.bitbucket

import uk.q3c.build.gitplus.remote.DefaultGitRemoteUrlMapper

/**
 * Created by David Sowerby on 26 Oct 2016
 */
class BitBucketUrlMapper : DefaultGitRemoteUrlMapper() {

    override fun cloneUrl(): String {
        return "https://${owner.repoUser}@${owner.providerBaseUrl}"
    }
}