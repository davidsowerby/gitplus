package uk.q3c.build.gitplus.remote

import spock.lang.Specification

/**
 * Created by David Sowerby on 03 Apr 2016
 */
class UnsupportedServiceProviderExceptionTest extends Specification {

    def "construct"() {
        given:
        UnsupportedServiceProviderException exception = new UnsupportedServiceProviderException(ServiceProvider.BITBUCKET)

        expect:
        exception.getMessage().equals('Service Provider not supported: BITBUCKET')
    }
}
