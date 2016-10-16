package uk.q3c.build.gitplus.local

import org.eclipse.jgit.lib.BranchConfig
import org.eclipse.jgit.lib.StoredConfig

/**
 * Created by David Sowerby on 06 Nov 2016
 */
interface BranchConfigProvider {
    fun get(cfg: StoredConfig, branch: String): BranchConfig
}