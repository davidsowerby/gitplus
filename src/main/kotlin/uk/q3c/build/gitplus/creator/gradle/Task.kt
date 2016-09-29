package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 02 Oct 2016
 */
class Task(name: String, val type: String = "", val dependsOn: String = "", val plugin: String = "") : VariableNamedBlock(name) {

    init {
        writeWhenEmpty = true
    }

    override fun openBlock() {
        fileBuffer.append("task ", name, "(")
        var precedingArgument: Boolean = false
        precedingArgument = addArgument("type", type, precedingArgument)
        precedingArgument = addArgument("dependsOn", dependsOn, precedingArgument)
        addArgument("plugin", plugin, precedingArgument)
        fileBuffer.appendLine(") {")
        fileBuffer.incrementIndent()
    }

    private fun addArgument(argumentName: String, argumentValue: String, precedingArgument: Boolean): Boolean {
        if (argumentValue.isNotEmpty()) {
            if (precedingArgument) {
                fileBuffer.append(", ")
            }
            fileBuffer.append(argumentName, ": ", argumentValue)
            return true
        }
        return false
    }


}


