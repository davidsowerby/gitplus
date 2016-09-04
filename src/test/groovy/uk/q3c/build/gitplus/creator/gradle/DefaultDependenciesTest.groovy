package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.KotlinObjectFactory

/**
 * Created by David Sowerby on 29 Aug 2016
 */
class DefaultDependenciesTest extends BlockReaderSpecification {

    DefaultDependencies<GradleFileContent> dependencies
    GradleFileContent gradleFileBuilder = Mock(GradleFileContent)
    final String krail = 'uk.q3c.krail:krail:0.9.9'
    final String logback = 'logback-classic'

    def setup() {
        dependencies = new DefaultDependencies<>(new DefaultScriptBlock<GradleFileContent>(gradleFileBuilder, DefaultScriptBlock.dependencies))
        KotlinObjectFactory.fileBuffer().reset()
    }

    def "dependency with exclude"() {
        given:
        final String scope = 'compile'

        when:
        Dependency dependency = dependencies.dependency(scope, krail).excludeModule(logback)

        then:
        dependencies.contains(dependency)

        when:
        dependencies.writeToBuffer()

        then:
        List<String> result = resultLines()
        result.get(0) == "dependencies {"
        result.get(1) == "    compile ('uk.q3c.krail:krail:0.9.9') {"
        result.get(2) == "        exclude module: 'logback-classic'"
        result.get(3) == "    }"
        result.get(4) == "}"
        result.get(5) == ""
        result.size() == 6


    }

    def "compile etc"() {
        when:
        dependencies.compile('dep1', 'dep2').runtime('dep3').integrationTestCompile('dep4', 'dep5').testCompile('dep6').dependencies('wiggly', 'dep6', 'dep7')
        dependencies.writeToBuffer()

        then:
        List<String> result = resultLines()
        result.get(0) == "dependencies {"
        result.get(1) == "    compile 'dep1'"
        result.get(2) == "    compile 'dep2'"
        result.get(3) == "    runtime 'dep3'"
        result.get(4) == "    integrationTestCompile 'dep4'"
        result.get(5) == "    integrationTestCompile 'dep5'"
        result.get(6) == "    testCompile 'dep6'"
        result.get(7) == "    wiggly 'dep6'"
        result.get(8) == "    wiggly 'dep7'"
        result.get(9) == "}"
        result.get(10) == ""
        result.size() == 11


    }


}
