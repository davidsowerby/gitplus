package uk.q3c.gitplus.remote

import spock.lang.Specification

import static uk.q3c.gitplus.remote.GitRemote.ServiceProvider.BITBUCKET

/**
 * Created by David Sowerby on 03 Apr 2016
 */
class UnsupportedServiceProviderExceptionTest extends Specification {

    def "construct"() {
        given:
        UnsupportedServiceProviderException exception = new UnsupportedServiceProviderException(BITBUCKET)

        expect:
        exception.getMessage().equals('Service Provider not supported: BITBUCKET')
    }
}
