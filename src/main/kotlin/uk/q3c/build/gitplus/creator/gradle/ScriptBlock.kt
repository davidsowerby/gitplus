package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 11 Sep 2016
 */
interface ScriptBlock<P> : ScriptElement {

    fun <E : ScriptElement> addElement(element: E): E
    fun end(): P
    fun lines(prefix: String = "", quoted: Boolean = false, vararg content: String): ScriptElement
    fun lines(vararg content: String): ScriptElement
    fun <E : ScriptElement> contains(element: E): Boolean
    fun setBlockHeading(value: (String) -> String)
    fun setAsLine(value: (String) -> String)
    fun indexOf(elementName: String): Int
    fun contains(elementName: String): Boolean
    fun setAlwaysABlock(value: Boolean)

    /**
     * Returns an instance for [elementName] (for example a [Repositories] instance for elementName == [DefaultScriptBlock.repositories]
     * The instance must be known to exist before calling this method - use [contains] first
     */
    fun getInstanceOf(elementName: String): ScriptElement

    fun setHolderOnly(value: Boolean)
    fun setOwner(value: ScriptElement)
}