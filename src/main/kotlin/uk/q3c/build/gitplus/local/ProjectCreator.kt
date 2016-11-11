package uk.q3c.build.gitplus.local

/**
 * Created by David Sowerby on 10 Nov 2016
 */
interface ProjectCreator {
    fun invoke(configuration: GitLocalConfiguration)
}