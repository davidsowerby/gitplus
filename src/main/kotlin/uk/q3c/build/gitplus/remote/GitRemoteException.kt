package uk.q3c.build.gitplus.remote

/**
 * Created by David Sowerby on 09 Mar 2016
 */
class GitRemoteException : RuntimeException {
    constructor(s: String, e: Exception) : super(s, e)

    constructor(s: String) : super(s)

}
