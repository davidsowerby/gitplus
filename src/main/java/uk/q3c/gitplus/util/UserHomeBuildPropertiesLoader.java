package uk.q3c.gitplus.util;

import com.google.common.collect.ImmutableList;
import uk.q3c.gitplus.gitplus.GitPlusConfigurationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads properties from user.home.gradle/gradle.properties
 * <p>
 * Created by David Sowerby on 11 Mar 2016
 */
public class UserHomeBuildPropertiesLoader implements BuildPropertiesLoader {
    private static final String BINTRAY_KEY = "bintrayKey";
    private static final String GITHUB_KEY_RESTRICTED = "githubKeyRestricted";
    private static final ImmutableList<String> requiredProperties = ImmutableList.of(BINTRAY_KEY, GITHUB_KEY_RESTRICTED);
    private static final String GITHUB_KEY_FULL_ACCESS = "gitHubKeyFullAccess";
    private Properties properties;

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public BuildPropertiesLoader load() throws IOException {
        File userHome = new File(System.getProperty("user.home"));
        File gradlePropertiesFile = new File(userHome, ".gradle/gradle.properties");
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(gradlePropertiesFile)) {
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
    public String bintrayKey() {
        return (String) properties.get(BINTRAY_KEY);
    }

    @Override
    public String githubKeyRestricted() {
        return (String) properties.get(GITHUB_KEY_RESTRICTED);
    }

    @Override
    public String githubKeyFullAccess() {
        return (String) properties.get(GITHUB_KEY_FULL_ACCESS);
    }
}
