package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.KotlinObjectFactory

/**
 * Created by David Sowerby on 04 Sep 2016
 */
class DefaultPluginsTest extends BlockReaderSpecification {

    Plugins plugins

    GradleFileContent gradleFileBuilder = Mock(GradleFileContent)

    def setup() {
        plugins = new DefaultPlugins(new DefaultScriptBlock<GradleFileContent>(gradleFileBuilder, DefaultScriptBlock.plugins))
        KotlinObjectFactory.fileBuffer().reset()
    }

    def "output"() {

        when:
        plugins.java().groovy().maven().mavenPublish().plugins('wiggly1', 'wiggly2').plugin('org.unbroken-dome.test-sets', '1.2.0').writeToBuffer()

        then:
        List<String> result = resultLines()
        result.get(0) == 'plugins {'
        result.get(1) == "    id 'java'"
        result.get(2) == "    id 'groovy'"
        result.get(3) == "    id 'maven'"
        result.get(4) == "    id 'maven-publish'"
        result.get(5) == "    id 'wiggly1'"
        result.get(6) == "    id 'wiggly2'"
        result.get(7) == "    id 'org.unbroken-dome.test-sets' version '1.2.0'"
        result.get(8) == '}'
        result.get(9) == ''
        result.size() == 10

    }

    def "parent"() {
        expect:
        plugins.end() == gradleFileBuilder
    }
}
