package uk.q3c.gitplus.remote;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by David Sowerby on 21 Mar 2016
 */
public class GitHubProvider {

    public GitHub get(@Nonnull String apiToken) throws IOException {
        checkNotNull(apiToken);
        Properties properties = new Properties();
        properties.put("oauth", apiToken);
        return GitHubBuilder.fromProperties(properties)
                            .withRateLimitHandler(RateLimitHandler.FAIL)
                            .build();

    }
}
