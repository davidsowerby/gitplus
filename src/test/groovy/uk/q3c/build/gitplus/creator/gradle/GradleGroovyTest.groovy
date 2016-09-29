package uk.q3c.build.gitplus.creator.gradle

import com.google.common.collect.ImmutableSet
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.KotlinObjectFactory
import uk.q3c.build.gitplus.creator.gradle.buffer.FileBuffer
import uk.q3c.build.gitplus.creator.gradle.element.BasicScriptElement
import uk.q3c.util.testutil.FileTestUtil
import uk.q3c.util.testutil.TestResource

/**
 * Created by David Sowerby on 29 Sep 2016
 */
class GradleGroovyTest extends Specification {

    GradleBuilder gradle
    @Rule
    TemporaryFolder temporaryFolder
    File temp
    File outputFile
    FileBuffer fileBuffer

    def setup() {
        gradle = new GradleGroovy()
        temp = temporaryFolder.root
        outputFile = new File(temp, 'build.gradle')
        fileBuffer = KotlinObjectFactory.fileBuffer()
        fileBuffer.reset()
    }

    def "publishing"() {
        given:
        gradle.mavenPublishing()

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'publishing.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()
    }

    def "empty config is written"() {
        given:
        gradle.config("testSets")

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'empty-config.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()
    }

    def "kotlin"() {
        given:
        gradle.kotlin('1.0.4')

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'kotlin.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()
    }

    def "dependencies with multiple excludes"() {
        gradle.dependencies().dependency('compile', 'dep1').excludeGroup('grp1').excludeGroup('grp2').excludeModule('mod1').excludeGroupModule('grp3', 'mod2')

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'dependencies.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()
    }


    def "test sets"() {
        given:
        gradle.testSets('unitTest', 'integrationTest', 'smokeTest')

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'testSets.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()

    }

    def "applyPlugin and applyFrom"() {
        given:
        gradle.applyPlugin('uk.q3c.simplycd')
        gradle.applyFrom('gradle/versioning.gradle')

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'apply.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()
    }

    def "task and wrapper task"() {
        given:
        gradle.task('taskWithAllAttributes', 'taskType', 'dependsOn', 'plugin')
        gradle.wrapper('2.10')

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'taskAndWrapper.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()
    }

    def "spock"() {
        given:
        gradle.spock("testCompile", "1.0-groovy-2.4")

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'spock.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()
    }


    def "'special' dependency, eg gradleApi() is not quoted"() {
        given:
        gradle.dependencies('compile').dependency('compile', 'gradleApi()')
        gradle.dependencies('compile').unaryPlus('otherApi()')

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'specialDependency.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()
    }


    def "base version, only one allowed, defaults to 0.0.1"() {
        given:
        gradle.baseVersion('0.0.1')

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'baseVersion.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()
    }

    def "Kotlin generated getters return same as fluent methods"() {
        expect:
        gradle.repositories() == gradle.getRepositories()
        gradle.dependencies() == gradle.getDependencies()
        gradle.getPlugins() instanceof Plugins
        gradle.getPlugins() == gradle.getPlugins() // returns same instance
        gradle.buildscript() == gradle.getBuildscript()
        gradle.buildscript().dependencies() == gradle.buildscript().getDependencies()
        gradle.buildscript().dependencies() == gradle.buildscript().getDependencies() // repeated to ensure single instance
        gradle.buildscript().repositories() == gradle.buildscript().getRepositories()
        gradle.buildscript().repositories() == gradle.buildscript().getRepositories() // repeated to ensure single instance
        gradle.getElements() != null
        gradle.getMavenLocal() == 'mavenLocal()'
        gradle.getFileBuffer() == fileBuffer

    }

    def "java adds plugin and sets sourceCompatibility"() {
        given:
        gradle.java('1.8')

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'java.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()
    }

    def "plugins with a varargs list"() {
        given:
        gradle.plugins('maven', 'maven-publish')
        gradle.plugins('java', 'groovy')

        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'plugins.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()
    }


    def "fullMonty"() {
        given:
        gradle.applyPlugin('uk.q3c.simplycd')
        gradle.applyFrom('gradle/versioning.gradle')
        gradle.mavenPublishing()
        gradle.kotlin('1.0.4')
        gradle.testSets('unitTest', 'integrationTest', 'smokeTest')
        gradle.spock('testCompile', '1.0-groovy-2.4')


        when:
        gradle.writeToFile(outputFile)

        then:
        File expected = TestResource.resource(this, 'fullMonty.gradle')
        !FileTestUtil.compareIgnoreBlankLines(outputFile, expected).isPresent()

    }

    def "equals and hashcode"() {
        given:
        def a = new BasicScriptElement("A")
        def a1 = new BasicScriptElement("A")
        def b = new BasicScriptElement("B")

        expect:
        a.equals(a1)
        !b.equals(a)
        a.hashCode() == a1.hashCode()
        !(b.hashCode() == a.hashCode())

    }

    def "script element in set"() {
        given:
        HashTest hashTest = new HashTest()

        when:
        hashTest.set1.add(new BasicScriptElement("D"))
        hashTest.set1.add(new BasicScriptElement("A"))
        hashTest.set1.add(new BasicScriptElement("B"))
        hashTest.set1.add(new BasicScriptElement("B"))

        then:
        hashTest.set1.size() == 3
        hashTest.set1.containsAll(ImmutableSet.of(new BasicScriptElement("A"), new BasicScriptElement("D"), new BasicScriptElement("B")))
    }


    def "Dependency unaryPlus throws Exception"() {
        when:
        gradle.dependencies('compile').dependency('compile', 'gradleApi()').unaryPlus("x")

        then:
        UnsupportedOperationException exception = thrown()
        exception.getMessage() == "'+' cannot be used inside a Dependency"
    }

    class HashTest {
        LinkedHashSet<BasicScriptElement> set1 = new LinkedHashSet<>()

        HashTest() {
            set1.add(new BasicScriptElement("A"))
        }

        Set<BasicScriptElement> getSet() {
            return set1
        }
    }

}
