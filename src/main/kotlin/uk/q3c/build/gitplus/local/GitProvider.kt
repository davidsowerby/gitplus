package uk.q3c.build.gitplus.local

import org.eclipse.jgit.api.Git

/**
 * Created by David Sowerby on 06 Nov 2016
 */
interface GitProvider {
    fun openRepository(localConfiguration: GitLocalConfiguration): Git
}