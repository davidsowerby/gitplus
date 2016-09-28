package uk.q3c.build.gitplus.creator.gradle

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import uk.q3c.KotlinObjectFactory
import uk.q3c.util.testutil.FileTestUtil
import uk.q3c.util.testutil.TestResource

/**
 * Created by David Sowerby on 12 Sep 2016
 */
class DefaultGradleFileContentTest extends BlockReaderSpecification {

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    GradleFileContent gradleFileContent
    ScriptBlock<GradleFile> scriptBlock
    GradleFile mainFile = Mock(GradleFile)

    def setup() {
        KotlinObjectFactory.fileBuffer().reset()
        buildFile = testProjectDir.newFile('build.gradle')
        scriptBlock = new DefaultScriptBlock<GradleFile>(mainFile, "mainFile")
        gradleFileContent = new DefaultGradleFileContent(scriptBlock)

    }


    def "all aspects"() {
        given:
        File expectedOutput = TestResource.resource(this, 'expected.gradle')
        gradleFileContent
                .buildscript()
                .repositories('mavenLocal()')
                .jcenter()
                .end()
                .dependencies()
                .compile('bsdep1').end()
                .end()

                .repositories('mavenLocal()').end()
                .dependencies()
                .compile('dep1', 'dep2', 'dep3')
                .runtime('dep4', 'dep5')
                .dependency('smokeCompile', 'blah').end()
                .end()
                .plugins('java', 'groovy').end()
                .dependencies()
                .compile('depA', 'depB')
                .end()
                .plugins('wiggly').end()
                .lines('group', true, 'uk.q3c.simplycd')
                .config('testSets').lines('line 1', 'line 2').end()
                .wrapper('2.10')
                .apply('wiggly')
                .applyFrom('wiggly.gradle')
                .task('hello').end()
                .task('hello2').type('Test').dependsOn('otherTask').end()
                .sourceCompatibility('1.8')
                .writeToFile(buildFile)

        expect:
        buildFile.exists()
        Optional<String> diffs = FileTestUtil.compare(buildFile, expectedOutput)
        if (diffs.isPresent()) println diffs.get()
        !diffs.isPresent()
    }

    def "return existing plugins element"() {
        given:
        gradleFileContent.plugins('plugin1')

        when:
        gradleFileContent.plugins('plugin2')
        gradleFileContent.writeToFile(buildFile)

        then:
        List<String> result = resultLines()
        result.get(0) == 'plugins {'
        result.get(1) == "    id 'plugin1'"
        result.get(2) == "    id 'plugin2'"
        result.get(3) == "}"
    }

    def "second call returns existing element instead of creating new"() {
        when:
        def p1 = gradleFileContent.plugins()
        def p2 = gradleFileContent.plugins()
        def r1 = gradleFileContent.repositories()
        def r2 = gradleFileContent.repositories()
        def b1 = gradleFileContent.buildscript()
        def b2 = gradleFileContent.buildscript()
        def d1 = gradleFileContent.dependencies()
        def d2 = gradleFileContent.dependencies()

        then:
        p1 == p2
        r1 == r2
        b1 == b2
        d1 == d2
    }
}
