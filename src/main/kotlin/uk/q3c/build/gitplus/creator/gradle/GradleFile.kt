package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.FileBuilder
import java.io.File

/**
 * Created by David Sowerby on 12 Sep 2016
 */
interface GradleFile : ScriptElement, FileBuilder {
     fun getContent(): GradleFileContent

    fun getFilename(): String
    fun setFilename(filename: String)
}