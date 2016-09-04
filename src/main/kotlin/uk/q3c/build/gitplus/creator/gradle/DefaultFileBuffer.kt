package uk.q3c.build.gitplus.creator.gradle

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.atomic.AtomicInteger


/**
 * Created by David Sowerby on 30 Aug 2016
 */

object DefaultFileBuffer : FileBuffer {

    private val newLine = "\n"
    private val space = ' '
    private val bufferStartCapacity = 500
    private val indentStartLevel = 0
    private val indentWidth = 4 // tests may fail if this is different to the IDE formatter

    private val log = LoggerFactory.getLogger(this.javaClass.name)


    private var currentIndent = AtomicInteger(indentStartLevel)
    private var buf = StringBuilder(bufferStartCapacity)


    override fun writeToFile(file: File) {
        log.debug("write buffer to file: {}", file)
        FileUtils.write(file, buf.toString())
    }


    override fun reset() {
        currentIndent = AtomicInteger(indentStartLevel)
        buf = StringBuilder(bufferStartCapacity)
        log.debug("DefaultFileBuffer reset")
    }


    override fun incrementIndent(): Int {
        log.debug("incrementing DefaultFileBuffer indent")
        return currentIndent.incrementAndGet()
    }


    override fun decrementIndent(): Int {
        log.debug("decrementing DefaultFileBuffer indent")
        return currentIndent.decrementAndGet()
    }


    override fun appendLine(vararg items: String) {
        log.debug("appending {} items, plus line terminator", items.size)
        indent()
        for (item in items) {
            buf.append(item)
        }
        buf.append(newLine)
    }

    override fun append(vararg items: String) {
        log.debug("appending {} items", items.size)
        indent()
        for (item in items) {
            buf.append(item)
        }
    }

    override fun blankLine() {
        buf.append("\n")
    }


    /**
     * Indents (by inserting into the buffer), the correct number of spaces for the [currentIndent] and [indentWidth]
     */
    private fun indent() {
        val spacesToIndent = (currentIndent.get() * indentWidth) - 1
        for (i in 0..spacesToIndent) {
            buf.append(space)
        }
    }

    override fun getCurrentIndent(): Int {
        return currentIndent.get()
    }


    override fun output(): String {
        return buf.toString()
    }
}
