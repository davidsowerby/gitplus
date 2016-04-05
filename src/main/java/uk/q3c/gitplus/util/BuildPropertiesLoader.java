package uk.q3c.gitplus.util;

import uk.q3c.gitplus.remote.GitRemote.ServiceProvider;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Properties;

/**
 * Loader for build properties, typically retrieved from gradle.properties, but other implementations may retrieve them from elsewhere. Its main purpose it
 * to provide API tokens.<p>
 * An attempt is made with this interface to restrict the most dangerous access (deleting remote repositories) - but it is up to the developer to ensure that
 * the necessary protection is in place to avoid accidental deletion of what is often the central repository, and that the tokens provided are generated with
 * the appropriate privileges<p>
 * <p>
 * Created by David Sowerby on 11 Mar 2016
 */
public interface BuildPropertiesLoader {
    /**
     * Returns the full properties object to enable access to any properties not explicitly covered by this API.  Call {@link #load()} first.
     *
     * @return Returns the full properties object to enable access to any properties not explicitly covered by this API
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
     * Returns the API token for Bintray.
     *
     * @return the API token for Bintray.
     */
    String bintrayToken();

    /**
     * Returns an API token.  It is expected that this token gives limited access rights.  Typically this would be enough
     * to raise issues, but exclude creating / deleting repositories.  It is up to the developer how best to use this and
     * {@link #apiTokenRepoCreate(ServiceProvider)}
     *
     * @return An API token with restricted privileges
     * @param serviceProvider the remote repository service provider (GitHub, BitBucket etc)
     */
    String apiTokenRestricted(@Nonnull ServiceProvider serviceProvider) throws IOException;

    /**
     * Returns an API token. It is expected that this token gives privileges to create repositories as well as the privileges provided by
     * {@link #apiTokenRestricted}
     *
     * @return an API token with restricted privileges plus repository create privilege.
     * @param serviceProvider the remote repository service provider (GitHub, BitBucket etc)
     */
    String apiTokenRepoCreate(@Nonnull ServiceProvider serviceProvider) throws IOException;

    /**
     * Returns an API token.  It is expected that this token gives privileges to delete repositories ONLY
     *
     * @param serviceProvider the remote repository service provider (GitHub, BitBucket etc)
     * @return an API token ONLY delete repository privileges
     */
    String apiTokenRepoDelete(@Nonnull ServiceProvider serviceProvider) throws IOException;

    /**
     * Returns the email of the user to associate with a Git tag
     *
     * @return the email of the user to associate with a Git tag
     */
    String taggerEmail() throws IOException;

    /**
     * Returns the name of the user to associate with a Git tag
     *
     * @return the name of the user to associate with a Git tag
     */
    String taggerName() throws IOException;
}
