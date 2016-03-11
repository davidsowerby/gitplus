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
     * Returns the API key for GitHub
     *
     * @return the API key for GitHub
     */
    String githubKey();
}
