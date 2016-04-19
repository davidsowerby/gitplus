package uk.q3c.build.gitplus

import spock.lang.Specification

import java.nio.file.Paths

/**
 * Created by David Sowerby on 18 Apr 2016
 */
class ResourceCheck extends Specification {

    def "resource"() {
        when:

        URL url = this.getClass()
                .getResource('changelog-step1.md');
        File expectedResult1 = Paths.get(url.toURI())
                .toFile();

        then:
        expectedResult1 != null
    }
}
