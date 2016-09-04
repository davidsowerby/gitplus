package uk.q3c.build.gitplus.creator.gradle

import org.slf4j.LoggerFactory


/**
 * Created by David Sowerby on 11 Sep 2016
 */
class DefaultScriptBlock<P>(val parent: P, val elementName: String) : ScriptBlock<P> {
    private val log = LoggerFactory.getLogger(this.javaClass.name)
    private val subElements: MutableList<ScriptElement> = mutableListOf()

    val fileBuffer: FileBuffer
    private var blockHeading: (name: String) -> String = { name -> name }
    private var asLine: (name: String) -> String = { name -> name }
    private var holderOnly: Boolean = false
    private lateinit var owner: ScriptElement
    private var alwaysABlock = false


    init {
        fileBuffer = DefaultFileBuffer
    }

    override fun setAlwaysABlock(value: Boolean) {
        alwaysABlock = value
    }

    override fun contains(elementName: String): Boolean {
        return indexOf(elementName) >= 0
    }

    override fun getInstanceOf(elementName: String): ScriptElement {
        val index = indexOf(elementName)
        return subElements[index]
    }

    override fun setOwner(value: ScriptElement) {
        owner = value
    }

    override fun setBlockHeading(value: (name: String) -> String) {
        blockHeading = value
    }

    override fun setAsLine(value: (name: String) -> String) {
        asLine = value
    }

    override fun setHolderOnly(value: Boolean) {
        holderOnly = value
    }

    override fun indexOf(elementName: String): Int {
        var index: Int = 0
        for (element in subElements) {
            if (elementName.equals(element.getName())) {
                return index
            }
            index++
        }
        return -1
    }

    override fun <E : ScriptElement> addElement(element: E): E {
        subElements.add(element)
        log.debug("Element added")
        return element
    }

    override fun end(): P {
        return parent
    }

    override fun <E : ScriptElement> contains(element: E): Boolean {
        return subElements.contains(element)
    }

    override fun getName(): String {
        return elementName
    }

    override fun writeToBuffer() {
        if (holderOnly) {
            writeSubElements()
        } else {
            if (alwaysABlock || isUsed()) {
                fileBuffer.append(blockHeading(elementName), openBlock())
                fileBuffer.incrementIndent()
                writeSubElements()
                fileBuffer.decrementIndent()
                fileBuffer.append(closeBlock())
            } else {
                fileBuffer.appendLine(asLine(elementName))
            }
        }
    }

    private fun writeSubElements() {
        for (element in subElements) {
            if (!(element is ScriptLine) && !(element is Dependency))
                fileBuffer.blankLine()
            element.writeToBuffer()
        }
    }

    fun isUsed(): Boolean {
        return subElements.isNotEmpty()
    }


    protected fun closeBlock(): String {
        return "}\n"
    }

    protected fun openBlock(): String {
        return " {\n"
    }


    override fun lines(prefix: String, quoted: Boolean, vararg content: String): ScriptElement {
        val p = if (prefix.isEmpty()) "" else prefix + " "
        for (contentLine in content) {
            val q = if (quoted) quoted(contentLine) else contentLine
            addElement(DefaultScriptLine(p + q))
        }
        return owner
    }

    override fun lines(vararg content: String): ScriptElement {
        for (contentLine in content) {
            addElement(DefaultScriptLine(contentLine))
        }
        return owner
    }


    companion object {
        val buildScript = "buildscript"
        val repositories = "repositories"
        val plugins = "plugins"
        val dependencies = "dependencies"
        val task = "task"
        val fileContent = "fileContent"

        val compile = "compile"
        val runtime = "runtime"
        val testCompile = "testCompile"
        val integrationTestCompile = "integrationTestCompile"

        val space = " "

        val eclipse = "eclipse-wtp"
        val idea = "idea"
        val mavenPublish = "maven-publish"
        val maven = "maven"
        val java = "java"
        val groovy = "groovy"

//        val kodein = Kodein{
//            bind()
//        }
    }


}