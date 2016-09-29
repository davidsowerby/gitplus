package uk.q3c.build.gitplus.creator.gradle

import spock.lang.Specification

/**
 * Created by David Sowerby on 07 Oct 2016
 */
class BuildscriptTest extends Specification {

    Buildscript buildscript

    def setup() {
        buildscript = new Buildscript()
    }

    def "repositories added once to elements and only once"() {
        when:

        true // do nothing

        then:
        buildscript.elements.isEmpty()

        when:
        Repositories repos = buildscript.repositories()

        then:
        buildscript.elements.size() == 1
        buildscript.elements.contains(repos)

        when:
        repos = buildscript.repositories()

        then:
        buildscript.elements.size() == 1
        buildscript.elements.contains(repos)

        when:
        repos = buildscript.getRepositories()

        then:
        buildscript.elements.size() == 1
        buildscript.elements.contains(repos)


    }
}
