package uk.q3c.gitplus.remote;

import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import uk.q3c.gitplus.gitplus.GitPlusConfiguration;
import uk.q3c.gitplus.gitplus.GitPlusConfigurationException;
import uk.q3c.gitplus.remote.GitRemote.TokenScope;

import javax.annotation.Nonnull;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by David Sowerby on 21 Mar 2016
 */
public class GitHubProvider {

    public Github get(@Nonnull GitPlusConfiguration configuration, @Nonnull TokenScope tokenScope) throws IOException {
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
        return new RtGithub(token);

    }
}
