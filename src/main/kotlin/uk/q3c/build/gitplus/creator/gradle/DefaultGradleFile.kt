package uk.q3c.build.gitplus.creator.gradle

import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

/**
 * Created by David Sowerby on 12 Sep 2016
 */
class DefaultGradleFile(val projectDir: File) : GradleFile {
    private val log = LoggerFactory.getLogger(this.javaClass.name)

    val fileBuffer: FileBuffer
    private var filename: String = "build.gradle"


    init {
        fileBuffer = DefaultFileBuffer
    }

    override fun write(): Optional<File> {
        try {
            fileContent.writeToBuffer()
            val file: File = File(projectDir, filename)
            fileBuffer.writeToFile(file)
            return Optional.of(file)
        } catch(e: Exception) {
            log.error("unable to write gradle file", e)
            return Optional.empty()
        }
    }


    override fun getName(): String {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val fileContent: GradleFileContent

    init {
        fileContent = ElementFactory.fileContent(this)
    }

    override fun writeToBuffer() {
        fileContent.writeToBuffer()
    }

    override fun getContent(): GradleFileContent {
        return fileContent
    }

    override fun getFilename(): String {
        return filename
    }

    override fun setFilename(filename: String) {
        this.filename = filename
    }

}