package uk.q3c.build.gitplus.test

import uk.q3c.build.gitplus.GitPlusFactory
import uk.q3c.build.gitplus.notSpecified
import uk.q3c.build.gitplus.remote.ServiceProvider.GITHUB

/**
 * Created by David Sowerby on 04 Sep 2017
 */
object TestEnvironmentCheck {
    var tokensNotAvailable: Boolean? = tokensNotAvailable()

    private fun tokensNotAvailable(): Boolean {
        try {
            val gitPlus = GitPlusFactory.getInstance()
            gitPlus.propertiesFromGradle()
            gitPlus.local.active = false
            gitPlus.remote.active = false
            gitPlus.execute()
            if (gitPlus.apiTokenIssueCreate(GITHUB) == notSpecified) {
                return true
            }
            if (gitPlus.apiTokenRepoCreate(GITHUB) == notSpecified) {
                return true
            }
            if (gitPlus.apiTokenRepoDelete(GITHUB) == notSpecified) {
                return true
            }
        } catch (exception: Exception) {
            return true
        }
        return false
    }
}
