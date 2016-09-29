package uk.q3c.build.gitplus.creator.gradle.element

import uk.q3c.build.gitplus.creator.gradle.buffer.DefaultFileBuffer
import uk.q3c.build.gitplus.creator.gradle.buffer.FileBuffer

/**
 * Created by David Sowerby on 07 Oct 2016
 */
open class BasicScriptElement(val content: String) : ScriptElement {
    val fileBuffer: FileBuffer = DefaultFileBuffer
    var formattedContent: String = ""

    override fun write() {
        formattedContent = formatContent()
        fileBuffer.appendLine(formattedContent)
    }

    open fun formatContent(): String {
        return content
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other is BasicScriptElement) {
            return content.equals(other.content)
        }
        return false
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }

    override fun toString(): String {
        return content
    }
}