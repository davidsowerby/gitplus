package uk.q3c.build.gitplus.creator.gradle

open class VariableNamedBlock(val name: String) : NamedBlock() {


    override fun blockName(): String {
        return name
    }
}