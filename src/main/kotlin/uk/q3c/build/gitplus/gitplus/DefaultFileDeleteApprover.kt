package uk.q3c.build.gitplus.gitplus

import java.io.File

/**
 * Created by David Sowerby on 10 Nov 2016
 */
class DefaultFileDeleteApprover : FileDeleteApprover {
    override fun approve(file: File): Boolean {
        return false
    }
}