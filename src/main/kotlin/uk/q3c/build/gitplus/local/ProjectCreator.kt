package uk.q3c.build.gitplus.local

/**
 * Creates a project.  See also https://github.com/davidsowerby/projectadmin for at least one other implementation
 *
 * Created by David Sowerby on 10 Nov 2016
 */
interface ProjectCreator {
    fun invoke(configuration: GitLocalConfiguration)
}