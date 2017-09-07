package uk.q3c.build.gitplus.remote

/**
 * This implementation would need to be replaced if there were more than one remote source (for example BitBucket)
 *
 *
 * Created by David Sowerby on 16 Mar 2016
 */
open class DefaultGitRemoteUrlMapper : GitRemoteUrlMapper {

    lateinit override var owner: GitRemoteConfiguration


    /**
     * This is the same for both GitHub and BitBucket
     */
    override fun repoBaselUrl(): String {
        return "https://${owner.providerBaseUrl}/${owner.remoteRepoFullName()}"
    }

    /**
     * Bitbucket example: 'https://dxsowerby@bitbucket.org/dxsowerby/cpas.git
     */
    override fun cloneUrl(): String {
        return "${repoBaselUrl()}.git"
    }


    override fun tagUrl(): String {
        return "${repoBaselUrl()}/tree/"
    }

    override fun issuesUrl(): String {
        return "${repoBaselUrl()}/issues/"
    }

    override fun wikiUrl(): String {
        return "${repoBaselUrl()}/wiki"
    }

    override fun wikiCloneUrl(): String {
        return "${repoBaselUrl()}.wiki.git"
    }

    override fun apiUrl(): String {
        return "https://api.github.com"
    }


}
