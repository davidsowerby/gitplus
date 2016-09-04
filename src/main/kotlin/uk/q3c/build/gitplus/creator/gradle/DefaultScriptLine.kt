package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 11 Sep 2016
 */
class DefaultScriptLine(val content: String) : ScriptLine {
    override fun getName(): String {
        return content
    }

    val fileBuffer: FileBuffer

    init {
        fileBuffer = DefaultFileBuffer
    }

    override fun writeToBuffer() {
        fileBuffer.appendLine(content)
    }

}