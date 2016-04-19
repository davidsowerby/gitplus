package uk.q3c.build.gitplus.remote;

import uk.q3c.build.gitplus.remote.GitRemote.ServiceProvider;

/**
 * Created by David Sowerby on 03 Apr 2016
 */
public class UnsupportedServiceProviderException extends RuntimeException {
    public UnsupportedServiceProviderException(ServiceProvider provider) {
        super("Service Provider not supported: " + provider.name());
    }
}
