package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.KotlinObjectFactory

/**
 * Created by David Sowerby on 05 Sep 2016
 */
class DefaultBuildScriptTest extends BlockReaderSpecification {

    BuildScript buildScript
    GradleFileContent gradleFileBuilder = Mock(GradleFileContent)

    def setup() {
        KotlinObjectFactory.fileBuffer().reset()
        buildScript = new DefaultBuildScript(new DefaultScriptBlock<GradleFileContent>(gradleFileBuilder, "buildscript"))
    }

    def "repositories and dependencies"() {
        when:
        buildScript.repositories().mavenLocal().end().dependencies().compile('a', 'b')
        buildScript.writeToBuffer()

        then:
        List<String> result = resultLines()
        result.get(0) == "buildscript {"
        result.get(1) == ""
        result.get(2) == "    repositories {"
        result.get(3) == "        mavenLocal()"
        result.get(4) == "    }"
        result.get(5) == ""
        result.get(6) == "    dependencies {"
        result.get(7) == "        compile 'a'"
        result.get(8) == "        compile 'b'"
        result.get(9) == "    }"
        result.get(10) == "}"
        result.get(11) == ""
        result.size() == 12
    }
}
