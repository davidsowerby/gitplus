package uk.q3c.build.gitplus.local

import uk.q3c.build.gitplus.remote.GitRemote

/**
 * Created by David Sowerby on 21 Oct 2016
 */
interface WikiLocal : GitLocal {
    fun prepare(remote: GitRemote, local: GitLocal)
}