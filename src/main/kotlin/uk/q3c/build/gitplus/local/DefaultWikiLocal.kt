package uk.q3c.build.gitplus.local

import com.google.inject.Inject
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.gitplus.GitPlus

/**
 * Created by David Sowerby on 21 Oct 2016
 */
class DefaultWikiLocal @Inject constructor(
        branchConfigProvider: BranchConfigProvider,
        gitProvider: GitProvider,
        localConfiguration: GitLocalConfiguration,
        gitInitChecker: GitInitChecker,
        gitCloner: GitCloner)

    : DefaultGitLocal(branchConfigProvider, gitProvider, localConfiguration, gitInitChecker, gitCloner),
        WikiLocal {


    private val log = LoggerFactory.getLogger(this.javaClass.name)

    init {
        active = false
    }

    override fun setOrigin() {
        try {
            val originUrl = parent.remote.wikiCloneUrl()
            log.debug("Setting local wiki origin to '{}'", originUrl)
            setOrigin(originUrl)
        } catch (e: Exception) {
            throw GitLocalException("Unable to set origin", e)
        }
    }

    override fun remoteUrl(): String {
        return parent.remote.wikiCloneUrl()
    }

    override fun prepare(parent: GitPlus) {
        if (active) {
            log.debug("preparing wiki element of GitLocal")
            projectName("${parent.local.projectName}.wiki")
            projectDirParent(parent.local.projectDirParent)
            super.prepare(parent)
        }
    }



    /**
     * A GitHub wiki has to be manually enabled on the GitHub site, before anything can be pushed to it.  This method will
     * swallow the exception thrown by a push if that has not happened, and logs as a warning.  At some point this really ought
     * to support a listener so that a UI application could be notified
     */
    override fun push(tags: Boolean, force: Boolean): PushResponse {
        log.debug("pushing wiki to remote, with tags = '{}'", tags)
        try {
            return super.push(tags, force)
        } catch (rre: GitLocalException) {
            log.warn("An exception was thrown when trying to push wiki content. if the remote is GitHub, a wiki has to be " +
                    "manually enabled.\n  There may be other reasons for the exception, but it is worth checking that the wiki" +
                    " is properly enabled", rre
            )
            return PushResponse().localFailure()
        }
    }

    override fun active(value: Boolean): GitLocalConfiguration {
        return configuration.active(value)
    }

}