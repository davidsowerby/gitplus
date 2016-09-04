package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.KotlinObjectFactory

/**
 * Created by David Sowerby on 06 Sep 2016
 */
class DefaultTaskTest extends BlockReaderSpecification {


    Task task
    GradleFileContent gradleFileContent = Mock(GradleFileContent)
    ElementFactory elementFactory

    def setup() {
        KotlinObjectFactory.fileBuffer().reset()
        elementFactory = KotlinObjectFactory.elementFactory()
    }

    def "with attributes"() {
        given:
        task = elementFactory.task(gradleFileContent, 'newTask')

        when:
        task.attribute('wiggly', 'wobble').dependsOn('otherTask').type('Copy').writeToBuffer()

        then:
        List<String> result = resultLines()
        result.get(0) == "task(name: newTask, wiggly: wobble, dependsOn: otherTask, type: Copy) {"
        result.get(1) == "}"
        result.get(2) == ""
        result.size() == 3
    }

    def "without attributes, with body"() {
        given:
        task = elementFactory.task(gradleFileContent, 'newTask')

        when:
        task.lines('line 1', 'line 2').writeToBuffer()

        then:
        List<String> result = resultLines()
        result.get(0) == "task(name: newTask) {"
        result.get(1) == "    line 1"
        result.get(2) == "    line 2"
        result.get(3) == "}"
        result.get(4) == ""
        result.size() == 5
    }


}
