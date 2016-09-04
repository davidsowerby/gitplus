package uk.q3c.build.gitplus.creator.gradle

import uk.q3c.KotlinObjectFactory

/**
 * Created by David Sowerby on 04 Sep 2016
 */
class DefaultRepositoriesTest extends BlockReaderSpecification {


    GradleFileContent gradleFileContent = Mock(GradleFileContent)
    Repositories<GradleFileContent> repos


    def setup() {
        KotlinObjectFactory.fileBuffer().reset()
        repos = new DefaultRepositories<>(new DefaultScriptBlock<GradleFileContent>(gradleFileContent, DefaultScriptBlock.repositories))
    }

    def "repositories and shortcuts"() {
        given:
        repos.repositories('a', 'b').jcenter().mavenCentral().mavenLocal()

        when:
        repos.writeToBuffer()

        then:
        List<String> results = resultLines()
        results.get(0) == 'repositories {'
        results.get(1) == '    a'
        results.get(2) == '    b'
        results.get(3) == '    jcenter()'
        results.get(4) == '    mavenCentral()'
        results.get(5) == '    mavenLocal()'
        results.get(6) == '}'
    }

    def "parent"() {
        expect:
        repos.end() == gradleFileContent
    }
}
