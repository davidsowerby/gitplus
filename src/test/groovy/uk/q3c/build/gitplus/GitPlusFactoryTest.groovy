package uk.q3c.build.gitplus

import spock.lang.Specification

/**
 * Created by David Sowerby on 21 Dec 2016
 */
class GitPlusFactoryTest extends Specification {


    def "getInstance"() {

        expect:
        GitPlusFactory.instance != null
    }
}
