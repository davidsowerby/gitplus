package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 12 Sep 2016
 */
class DefaultConfig(val block: ScriptBlock<GradleFileContent>) : Config, ScriptBlock<GradleFileContent> by block {

    init {
        block.setOwner(this)
    }


}