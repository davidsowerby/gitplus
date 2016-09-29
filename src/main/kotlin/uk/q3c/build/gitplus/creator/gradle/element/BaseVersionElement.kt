package uk.q3c.build.gitplus.creator.gradle.element

class BaseVersionElement(version: String) : BasicScriptElement(version) {
    override fun write() {
        fileBuffer.appendLine("ext.baseVersion = '$content'")
    }
}