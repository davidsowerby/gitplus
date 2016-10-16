package uk.q3c.build.gitplus.remote.github

import uk.q3c.build.gitplus.remote.DefaultGitRemoteUrlMapper

/**
 * Created by David Sowerby on 26 Oct 2016
 */
class GitHubUrlMapper : DefaultGitRemoteUrlMapper() {


    companion object {

        val STATUS_API_URL = "https://status.github.com/api/status.json"

    }
}