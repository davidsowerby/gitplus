package uk.q3c.gitplus.remote;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;
import uk.q3c.gitplus.gitplus.GitPlusConfiguration;
import uk.q3c.gitplus.gitplus.GitPlusConfigurationException;
import uk.q3c.gitplus.remote.GitRemote.TokenScope;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by David Sowerby on 21 Mar 2016
 */
public class GitHubProvider {

    public GitHub get(@Nonnull GitPlusConfiguration configuration, @Nonnull TokenScope tokenScope) throws IOException {
        checkNotNull(configuration);
        checkNotNull(tokenScope);
        String token;
        switch (tokenScope) {
            case RESTRICTED:
                token = configuration.getApiTokenRestricted();
                break;
            case CREATE_REPO:
                token = configuration.getApiTokenCreateRepo();
                break;
            case DELETE_REPO:
                token = configuration.getApiTokenDeleteRepo();
                break;
            default:
                throw new GitPlusConfigurationException("Unrecognised TokenScope");
        }
        Properties properties = new Properties();
        properties.put("oauth", token);
        return GitHubBuilder.fromProperties(properties)
                            .withRateLimitHandler(RateLimitHandler.FAIL)
                            .build();
    }
}
