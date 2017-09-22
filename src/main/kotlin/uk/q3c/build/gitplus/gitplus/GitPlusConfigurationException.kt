package uk.q3c.build.gitplus.gitplus

/**
 * Created by David Sowerby on 14 Mar 2016
 */
class GitPlusConfigurationException : RuntimeException {
    constructor(msg: String) : super(msg)

    constructor(s: String, e: Exception) : super(s, e)

}
