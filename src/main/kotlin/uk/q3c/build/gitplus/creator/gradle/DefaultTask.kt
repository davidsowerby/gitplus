package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 12 Sep 2016
 */
class DefaultTask(block: ScriptBlock<GradleFileContent>, taskName: String) : Task, ScriptBlock<GradleFileContent> by block {

    private var attributes: MutableMap<String, String> = mutableMapOf()

    init {
        block.setOwner(this)
        setBlockHeading { name -> heading() }
        attributes.put("name", taskName)
        setAlwaysABlock(true)
    }


    private fun heading(): String {
        val buffer: StringBuilder = StringBuilder("tasks.create(")
        var first = true
        for ((key, value) in attributes) {
            if (first)
                first = false
            else
                buffer.append(", ")
            buffer.append(key, ": ", value)
        }
        buffer.append(")")
        return buffer.toString()
    }

    override fun dependsOn(dependsOn: String): Task {
        attributes.put("dependsOn", dependsOn)
        return this
    }

    override fun type(type: String): Task {
        attributes.put("type", type)
        return this
    }

    override fun attribute(attributeName: String, attributeValue: String): Task {
        attributes.put(attributeName, attributeValue)
        return this
    }
}