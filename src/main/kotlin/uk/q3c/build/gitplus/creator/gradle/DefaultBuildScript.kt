package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.dependencies
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock.Companion.repositories


/**
 * Created by David Sowerby on 12 Sep 2016
 */
class DefaultBuildScript(block: ScriptBlock<GradleFileContent>) : BuildScript, ScriptBlock<GradleFileContent> by block {

    init {
        block.setOwner(this)
    }

    override fun repositories(vararg repositoryNames: String): Repositories<BuildScript> {
        @Suppress("UNCHECKED_CAST")
        val repositories1: Repositories<BuildScript> =
                if (contains(repositories))
                    getInstanceOf(repositories) as Repositories<BuildScript>
                else
                    addElement(ElementFactory.repositories(this)) as Repositories<BuildScript>

        repositories1.repositories(*repositoryNames)
        return repositories1
    }

    override fun dependencies(): Dependencies<BuildScript> {
        @Suppress("UNCHECKED_CAST")
        val instance: Dependencies<BuildScript> =
                if (contains(dependencies))
                    getInstanceOf(dependencies) as Dependencies<BuildScript>
                else
                    addElement(ElementFactory.dependencies(this)) as Dependencies<BuildScript>

        return instance
    }


}