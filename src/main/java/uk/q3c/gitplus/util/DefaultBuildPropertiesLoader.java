package uk.q3c.gitplus.util;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads properties from user.home.gradle/gradle.properties
 * <p>
 * Created by David Sowerby on 11 Mar 2016
 */
public class DefaultBuildPropertiesLoader implements BuildPropertiesLoader {
    private static final String BINTRAY_KEY = "bintrayKey";
    private static final String GITHUB_KEY = "githubKey";
    public final static ImmutableList<String> requiredProperties = ImmutableList.of(BINTRAY_KEY, GITHUB_KEY);

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
        properties.load(new FileInputStream(gradlePropertiesFile));
        checkForRequiredProperties();
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
    public String githubKey() {
        return (String) properties.get(GITHUB_KEY);
    }
}
