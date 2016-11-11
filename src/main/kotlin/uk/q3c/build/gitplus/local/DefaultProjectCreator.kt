package uk.q3c.build.gitplus.local

import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Adds a README file containing the project name, to the project directory
 *
 * Created by David Sowerby on 10 Nov 2016
 */
class DefaultProjectCreator : ProjectCreator {

    override fun invoke(configuration: GitLocalConfiguration) {
        FileUtils.write(File(configuration.projectDir(), "# README.md"), configuration.projectName)
    }
}