package uk.q3c.build.gitplus.remote


/**
 * Created by David Sowerby on 03 Apr 2016
 */
class UnsupportedServiceProviderException(provider: ServiceProvider) : RuntimeException("Service Provider not supported: " + provider.name)
