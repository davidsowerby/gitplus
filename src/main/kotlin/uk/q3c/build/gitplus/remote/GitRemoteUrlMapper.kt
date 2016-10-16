package uk.q3c.build.gitplus.remote

/**
 * Created by David Sowerby on 16 Mar 2016
 */
interface GitRemoteUrlMapper {

    var parent: GitRemote

    /**
     * The base url for the repo being used, typically derived from [repoUser] and [repoName].  For GitHub, for example,
     * this might be: 'https://github.com/davidsowerby/krail'
     */
    fun repoBaselUrl(): String

    /**
     * The url to (http) clone this repo, typically derived from [repoUser] and [repoName].  For GitHub, for example,
     * this might be: 'https://github.com/davidsowerby/krail.git'
     */
    fun cloneUrl(): String

    /**
     * The url to a specific tag in this repo, typically derived from [repoUser] and [repoName].  For GitHub, for example,
     * this might be: 'https://github.com/davidsowerby/krail/tree/'  (including the trailing slash)
     */
    fun tagUrl(): String

    /**
     * The url to the wiki for this repo, typically derived from [repoUser] and [repoName].  For GitHub, for example,
     * this might be: 'https://github.com/davidsowerby/krail/wiki'
     */
    fun wikiUrl(): String

    /**
     * The api url for the remote service provider.  Default is 'https://api.github.com/'
     */
    fun apiUrl(): String

    /**
     * The url to clone the wiki.  For example: 'https://github.com/davidsowerby/krail.wiki.git'
     */
    fun wikiCloneUrl(): String

    /**
     * The url to the issues for this repo, typically derived from [repoUser] and [repoName].  For GitHub, for example,
     * this might be: 'https://github.com/davidsowerby/krail/issues/'  (including the trailing slash)
     */
    fun issuesUrl(): String
}
