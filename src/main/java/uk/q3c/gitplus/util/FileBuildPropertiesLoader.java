package uk.q3c.gitplus.util;

import uk.q3c.gitplus.gitplus.GitPlusConfigurationException;
import uk.q3c.gitplus.remote.GitRemote.ServiceProvider;
import uk.q3c.gitplus.remote.UnsupportedServiceProviderException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads properties from a File source, which by default is user/home/.gradle/gradle.properties, but can be set
 * <p>
 * Created by David Sowerby on 11 Mar 2016
 */
public class FileBuildPropertiesLoader implements BuildPropertiesLoader {
    private static final String BINTRAY_TOKEN = "bintrayToken";
    private static final String TAGGER_NAME = "taggerName";
    private static final String TAGGER_EMAIL = "taggerEmail";
    private static final String GITHUB_TOKEN_RESTRICTED = "githubApiTokenRestricted";
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
        } catch (Exception e) {
            throw new GitPlusConfigurationException("Unable to load build properties", e);
        }
        return this;
    }

    @Override
    public String bintrayToken() {
        return (String) properties.get(BINTRAY_TOKEN);
    }

    @Override
    public String apiTokenRestricted(@Nonnull ServiceProvider serviceProvider) throws IOException {
        if (serviceProvider == ServiceProvider.GITHUB) {
            return retrieveValue(GITHUB_TOKEN_RESTRICTED);
        } else {
            throw new UnsupportedServiceProviderException(serviceProvider);
        }
    }

    @Override
    public String apiTokenRepoCreate(@Nonnull ServiceProvider serviceProvider) throws IOException {
        if (serviceProvider == ServiceProvider.GITHUB) {
            return retrieveValue(GITHUB_TOKEN_CREATE_REPO);
        } else {
            throw new UnsupportedServiceProviderException(serviceProvider);
        }
    }

    @Override
    public String apiTokenRepoDelete(@Nonnull ServiceProvider serviceProvider) throws IOException {
        if (serviceProvider == ServiceProvider.GITHUB) {
            return retrieveValue(GITHUB_TOKEN_DELETE_REPO);
        } else {
            throw new UnsupportedServiceProviderException(serviceProvider);
        }
    }

    @Override
    public String taggerEmail() throws IOException {
        return retrieveValue(TAGGER_EMAIL);
    }

    @Override
    public String taggerName() throws IOException {
        return retrieveValue(TAGGER_NAME);
    }

    private String retrieveValue(String key) throws IOException {
        if (properties == null) {
            load();
        }
        Object value = properties.get(key);
        if (value == null) {
            throw new MissingPropertyException(key);
        }
        return (String) value;
    }
}
