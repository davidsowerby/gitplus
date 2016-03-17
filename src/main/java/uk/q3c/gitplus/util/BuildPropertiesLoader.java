package uk.q3c.gitplus.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by David Sowerby on 11 Mar 2016
 */
public interface BuildPropertiesLoader {
    /**
     * Returns the full properties object to enable access to any properties not explicityly covered by this API.  Call {@link #load()} first.
     *
     * @return Returns the full properties object to enable access to any properties not explicityly covered by this API
     */
    Properties getProperties();

    /**
     * Loads the properties from wherever the implementation decides
     *
     * @return this for fluency
     * @throws IOException if the properties cannot be loaded, or any required property is missing
     */
    BuildPropertiesLoader load() throws IOException;

    /**
     * Returns the API key for Bintray.
     *
     * @return the API key for Bintray.
     */
    String bintrayKey();

    /**
     * Returns the API key for GitHub.  It is expected - but cannot be enforced - that this token gives limited access rights.  Typically this would be enough
     * to raise issues, but exclude creating / deleting repositories.  It is up to the developer how best to use this and {@link #githubKeyFullAccess()}
     *
     * @return the restricted API key for GitHub
     */
    String githubKeyRestricted();

    /**
     * Returns the API key for GitHub.  It is expected - but cannot be enforced - that this token gives full access to all privileges. It is up to the
     * developer how best to use this and {@link #githubKeyRestricted()}
     *
     * @return the full access API key for GitHub
     */
    String githubKeyFullAccess();
}
