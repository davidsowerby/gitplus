package uk.q3c.build.gitplus.gitplus

import java.io.File

/**
 * Enables a check to be made before deleting a directory or file.  For example, [GitLocal.cloneRemote] may use use FileUtils.forceDelete to remove an
 * existing
 * clone.   That is not good if you have set [GitPlusConfiguration.projectDir] to your home directory by mistake .....
 *
 *
 * Created by David Sowerby on 09 Apr 2016
 */
@FunctionalInterface
interface FileDeleteApprover {

    fun approve(file: File): Boolean
}
