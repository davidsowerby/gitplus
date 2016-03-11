package uk.q3c.gitplus.origin;

import com.google.common.collect.ImmutableList;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import uk.q3c.gitplus.util.PropertyException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by David Sowerby on 12 Feb 2016
 */
public class GitHubServiceApi implements OriginServiceApi {
    public enum Status {GREEN, YELLOW, RED}

    private static final ImmutableList<String> fixWords = ImmutableList.of("fix", "fixes", "fixed", "resolve", "resolves", "resolved", "close", "closes",
            "closed");
    private static Logger log = getLogger(GitHubServiceApi.class);
    private String repoName;
    private GHRepository repository;
    private String apiToken;
    private GitHub gitHub;

    public String getApiToken() {
        propertyCheck("apiToken", apiToken);
        return apiToken;
    }

    @Override
    public void setApiToken(@Nonnull String apiToken) {
        checkNotNull(apiToken);
        this.apiToken = apiToken;
        Properties properties = new Properties();
        properties.put("oauth", apiToken);
        try {
            gitHub = GitHubBuilder.fromProperties(properties)
                                  .build();
        } catch (IOException e) {
            throw new OriginServiceException("Unable to construct GitHub instance", e);
        }
    }

    @Override
    public String getRepoName() {
        propertyCheck("repoName", repoName);
        return repoName;
    }

    @Override
    public void setRepoName(@Nonnull String repoName) {
        checkNotNull(repoName);
        this.repoName = repoName;
    }

    @Override
    public boolean isIssueFixWord(String word) {
        if (word == null) {
            return false;
        }
        return fixWords.contains(word.toLowerCase());
    }


    @Override
    public GHIssue getIssue(int issueNumber) throws IOException {
        return getIssue(getRepoName(), issueNumber);
    }

    @Override
    public GHIssue getIssue(@Nonnull String repoName, int issueNumber) throws IOException {
        checkNotNull(repoName);
        log.debug("Retrieving issue {} from {}", issueNumber, repoName);
        return gitHub.getRepository(repoName)
                     .getIssue(issueNumber);
    }

    private void propertyCheck(String propertyName, String propertyValue) {
        if (propertyValue == null) {
            throw new PropertyException(propertyName);
        }
    }


    public Status apiStatus() {
        final String uri = "https://status.github.com/api/status.json";
        try {
            Response response = requestWithAuthorisation(uri)
                    .method(Request.GET)
                    .fetch();
            String status = response.as(JsonResponse.class)
                                    .json()
                                    .readObject()
                                    .getString("status");
            switch (status) {
                case "good":
                    return Status.GREEN;
                case "minor":
                    return Status.YELLOW;
                case "major":
                    return Status.RED;
                default:
                    return Status.RED;
            }
        } catch (IOException e) {
            log.error("Unable to retrieve status from online service");
            return Status.RED;
        }
    }

    public int remainingCalls() {
        int remaining = rateLimitInfo("remaining");
        return remaining;
    }

    public int timeOfRateLimitReset() {
        return rateLimitInfo("reset");
    }

    private int rateLimitInfo(String attribute) {
        final String uri = "https://api.github.com/rate_limit";
        try {
            Response response = requestWithAuthorisation(uri)
                    .method(Request.GET)
                    .fetch();
            return response.as(JsonResponse.class)
                           .json()
                           .readObject()
                           .getJsonObject("rate")
                           .getInt(attribute);
        } catch (IOException e) {
            log.error("Unable to access rate limit", e);
            return -1;
        }
    }

    private Request requestWithAuthorisation(String uri) {
        return new JdkRequest(uri).header("Accept", "application/vnd.github.v3+json")
                                  .header("Authorization", "token " + getApiToken());
    }


    public GHIssue createIssue(@Nonnull String issueTitle, @Nonnull String body, @Nonnull String label) throws
            IOException {
        checkNotNull(issueTitle);
        checkNotNull(body);
        checkNotNull(label);
        return getRepository().createIssue(issueTitle)
                              .body(body)
                              .assignee(getGitHub().getMyself())
                              .label(label)
                              .create();

    }

    protected GitHub getGitHub() throws IOException {
        if (gitHub == null) {
            Properties properties = new Properties();
            properties.put("oauth", getApiToken());
            gitHub = GitHubBuilder.fromProperties(properties)
                                  .withRateLimitHandler(RateLimitHandler.FAIL)
                                  .build();
        }
        return gitHub;
    }


    public GHRepository getRepository() throws IOException {
        if (repository == null) {
            repository = getGitHub().getRepository(getRepoName());
        }
        return repository;
    }
}
