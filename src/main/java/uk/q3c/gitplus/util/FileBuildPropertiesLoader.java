package uk.q3c.gitplus.util;

import com.google.common.collect.ImmutableList;
import uk.q3c.gitplus.gitplus.GitPlusConfigurationException;
import uk.q3c.gitplus.remote.GitRemote.ServiceProvider;
import uk.q3c.gitplus.remote.UnsupportedServiceProviderException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.q3c.gitplus.remote.GitRemote.ServiceProvider.GITHUB;

/**
 * Loads properties from a File source, which by default is user/home/.gradle/gradle.properties, but can be set
 * <p>
 * Created by David Sowerby on 11 Mar 2016
 */
public class FileBuildPropertiesLoader implements BuildPropertiesLoader {
    private static final String BINTRAY_TOKEN = "bintrayToken";
    private static final String GITHUB_TOKEN_RESTRICTED = "githubApiTokenRestricted";
    private static final ImmutableList<String> requiredProperties = ImmutableList.of(BINTRAY_TOKEN, GITHUB_TOKEN_RESTRICTED);
    private static final String GITHUB_TOKEN_CREATE_REPO = "gitHubApiTokenCreateRepo";
    private static final String GITHUB_TOKEN_DELETE_REPO = "gitHubApiTokenDeleteRepo";
    private File source;
    private Properties properties;

    public File getSource() {
        return source;
    }

    public FileBuildPropertiesLoader source(File source) {
        this.source = source;
        return this;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public BuildPropertiesLoader load() throws IOException {
        if (source == null) {
            File userHome = new File(System.getProperty("user.home"));
            source = new File(userHome, ".gradle/gradle.properties");
        }

        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(source)) {
            properties.load(fis);
            checkForRequiredProperties();
        } catch (Exception e) {
            throw new GitPlusConfigurationException("Unable to load build properties", e);
        }
        return this;
    }

    private void checkForRequiredProperties() throws IOException {
        for (String p : requiredProperties) {
            if (!properties.containsKey(p)) {
                throw new IOException("Required property not present: " + p);
            }
        }
    }

    @Override
    public String bintrayToken() {
        return (String) properties.get(BINTRAY_TOKEN);
    }

    @Override
    public String apiTokenRestricted(@Nonnull ServiceProvider serviceProvider) throws IOException {
        return retrieveToken(serviceProvider, GITHUB_TOKEN_RESTRICTED);
    }

    @Override
    public String apiTokenRepoCreate(@Nonnull ServiceProvider serviceProvider) throws IOException {
        return retrieveToken(serviceProvider, GITHUB_TOKEN_CREATE_REPO);
    }

    @Override
    public String apiTokenRepoDelete(@Nonnull ServiceProvider serviceProvider) throws IOException {
        return retrieveToken(serviceProvider, GITHUB_TOKEN_DELETE_REPO);
    }

    private String retrieveToken(ServiceProvider serviceProvider, String key) throws IOException {
        checkNotNull(serviceProvider);
        if (properties == null) {
            load();
        }
        if (serviceProvider == GITHUB) {
            Object value = properties.get(key);
            if (value == null) {
                throw new MissingPropertyException(key);
            }
            return (String) value;
        }
        throw new UnsupportedServiceProviderException(serviceProvider);
    }
}
