package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.build.gitplus.creator.gradle.buffer.DefaultFileBuffer
import uk.q3c.build.gitplus.creator.gradle.buffer.FileBuffer
import uk.q3c.build.gitplus.creator.gradle.element.BasicScriptElement
import uk.q3c.build.gitplus.creator.gradle.element.ScriptElement

abstract class NamedBlock : ScriptElement {
    val fileBuffer: FileBuffer = DefaultFileBuffer
    val elements: MutableSet<ScriptElement> = mutableSetOf()
    var writeWhenEmpty = false

    open fun openBlock() {
        fileBuffer.appendLine(blockName(), " {")
        fileBuffer.incrementIndent()
    }

    fun closeBlock() {
        fileBuffer.decrementIndent()
        fileBuffer.appendLine("}")
    }

    /**
     * Write the content of this block together with the block opening and closing.  Note that nothing at all is written if the block is empty
     */
    open fun writeBlockToBuffer() {
        fileBuffer.blankLine()
        if (writeWhenEmpty || this.isNotEmpty()) {
            openBlock()
            writeContent()
            closeBlock()
        }
    }

    fun isNotEmpty(): Boolean {
        return elements.isNotEmpty()
    }

    open fun writeContent() {
        for (element in elements) {
            element.write()
        }
    }


    override fun write() {
        writeBlockToBuffer()
    }

    abstract fun blockName(): String


    open operator fun String.unaryPlus() {
        elements.add(BasicScriptElement(this))
    }
}