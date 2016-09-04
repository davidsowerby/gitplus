package uk.q3c.build.gitplus.creator.gradle

import java.io.File

/**
 * Created by David Sowerby on 02 Sep 2016
 */
interface FileBuffer {

    /**
     * Writes the buffer to [file]
     *
     * @param file the file to write to
     * @throws IOException if the write fails
     */
    fun writeToFile(file: File)

    /**
     * Resets the current indent to 0 and clears the buffer
     */
    fun reset()

    /**
     * Increments the current indent
     */
    fun incrementIndent(): Int

    /**
     * Decrements the current indent
     */
    fun decrementIndent(): Int

    /**
     * Appends 1 or more items to the buffer (effectively concatenating them), terminated with an end of line character
     *
     * @param items the items to add
     */
    fun appendLine(vararg items: String)

    /**
     * The current indent.  This is multiplied by the indent level
     */
    fun getCurrentIndent(): Int

    /**
     * returns the buffer output as a String.
     *
     * @return the buffer output as a String.
     */
    fun output(): String

    /**
     * Same as [appendLine] except no line terminator is added
     */
    fun append(vararg items: String)

    /**
     * Adds a blank line without any indent (avoids comparison errors when 'blank' lines actually have spaces in them)
     */
    fun blankLine()
}