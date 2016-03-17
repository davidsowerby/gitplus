package uk.q3c.gitplus.util

import spock.lang.Specification

/**
 * Created by David Sowerby on 22 Mar 2016
 */
class UserHomeBuildPropertiesLoaderTest extends Specification {

    def "get properties"() {
        expect:
        new UserHomeBuildPropertiesLoader().load().getProperties().size() >= 2
        new UserHomeBuildPropertiesLoader().load().bintrayKey() != null
    }
}
