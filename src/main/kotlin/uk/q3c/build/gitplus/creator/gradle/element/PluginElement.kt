package uk.q3c.build.gitplus.creator.gradle.element

class PluginElement(content: String) : BasicScriptElement(content), ScriptElement {

    override fun formatContent(): String {
        if (content.contains(" version ")) {
            val split: List<String> = content.replace("'", "").split(" version ")
            val part1 = split.get(0)
            val part2 = split.get(1)
            return "id '$part1' version '$part2'"
        } else {
            return "id '$content'"
        }
    }
}