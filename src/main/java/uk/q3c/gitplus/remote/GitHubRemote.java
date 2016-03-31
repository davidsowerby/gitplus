package uk.q3c.gitplus.remote;

import com.google.common.collect.ImmutableList;
import com.jcabi.http.Request;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import uk.q3c.gitplus.gitplus.GitPlusConfiguration;

import javax.annotation.Nonnull;
import javax.json.JsonReader;
import java.io.IOException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by David Sowerby on 12 Feb 2016
 */
public class GitHubRemote implements GitRemote {
    public enum Status {GREEN, YELLOW, RED}

    public static final String STATUS_API_URL = "https://status.github.com/api/status.json";
    private static final ImmutableList<String> fixWords = ImmutableList.of("fix", "fixes", "fixed", "resolve", "resolves", "resolved", "close", "closes",
            "closed");
    private static Logger log = getLogger(GitHubRemote.class);
    @Nonnull
    private final RemoteRequest remoteRequest;
    private final GitHubProvider gitHubProvider;
    private GHRepository repository;
    private GitHub gitHub;
    private GitPlusConfiguration configuration;

    public GitHubRemote(@Nonnull GitPlusConfiguration gitPlusConfiguration, @Nonnull GitHubProvider gitHubProvider, @Nonnull RemoteRequest remoteRequest) throws
            IOException {
        checkNotNull(remoteRequest);
        checkNotNull(gitHubProvider);
        checkNotNull(gitPlusConfiguration);
        this.configuration = gitPlusConfiguration;
        this.remoteRequest = remoteRequest;
        this.gitHubProvider = gitHubProvider;
    }


    @Override
    public boolean isIssueFixWord(String word) {
        //noinspection SimplifiableIfStatement clearer in this form
        if (word == null) {
            return false;
        }
        return fixWords.contains(word.toLowerCase());
    }


    @Override
    public Issue getIssue(int issueNumber) {
        return getIssue(configuration.getRemoteRepoFullName(), issueNumber);
    }

    @Override
    public Issue getIssue(String repoName, int issueNumber) {
        log.debug("Retrieving issue {} from {}", issueNumber, repoName);
        try {
            String rName = (repoName == null || repoName.isEmpty()) ? configuration.getRemoteRepoFullName() : repoName;
            return new Issue(getGitHub().getRepository(rName)
                                        .getIssue(issueNumber));
        } catch (Exception e) {
            throw new GitRemoteException("Unable to retrieve issue " + issueNumber + " from " + repoName, e);
        }
    }

    @Override
    public CredentialsProvider getCredentialsProvider() {
        if (configuration.getApiToken() != null) {
            return new UsernamePasswordCredentialsProvider(configuration.getApiToken(), "");
        }
        throw new GitRemoteException("An api token must be provided in order to provide credentials");
    }

    @Override
    public Status apiStatus() {

        try {
            JsonReader response = remoteRequest.request(Request.GET, STATUS_API_URL, configuration.getApiToken());
            String status = response
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
            log.error("Unable to retrieve status from online service", e);
            return Status.RED;
        }
    }


    @Override
    public GHIssue createIssue(@Nonnull String issueTitle, @Nonnull String body, @Nonnull String label) throws
            IOException {
        checkNotNull(issueTitle);
        checkNotNull(body);
        checkNotNull(label);
        return getRepo().createIssue(issueTitle)
                        .body(body)
                        .assignee(getGitHub().getMyself())
                        .label(label)
                        .create();

    }

    protected GitHub getGitHub() throws IOException {
        if (gitHub == null) {
            this.gitHub = gitHubProvider.get(configuration.getApiToken());
        }
        return gitHub;
    }


    public GHRepository getRepo() throws IOException {
        if (repository == null) {
            repository = getGitHub().getRepository(configuration.getRemoteRepoFullName());
        }
        return repository;
    }

    /**
     * Creates the remote repo from the information in {@link #configuration}.  Note that the {@link GitHub#createRepository} methods called does not allow
     * for any option to control creation of a wiki - it is always created, even if {@link #configuration} has useWiki set to false.
     */
    @Override
    public void createRepo() {
        try {
            this.repository = getGitHub().createRepository(configuration.getProjectName(), configuration.getProjectDescription(), configuration
                            .getProjectHomePage(),
                    configuration.isPublicProject());
        } catch (Exception e) {
            throw new GitRemoteException("Unable to create Repo", e);
        }
    }


    @Override
    public void deleteRepo() {
        String confirmationMessage = "I really, really want to delete the " + configuration.getRemoteRepoFullName() + " repo from GitHub";
        try {
            if (confirmationMessage.equals(configuration.getConfirmRemoteDelete())) {
                getRepo().delete();
            } else {
                throw new GitRemoteException("Repo deletion not confirmed");
            }
        } catch (Exception e) {
            throw new GitRemoteException("Unable to delete remote repo", e);
        }
    }

    @Override
    public String getRepoName() {
        return configuration.getRemoteRepoFullName();
    }


    @Override
    public Set<String> listRepositoryNames() throws IOException {
        return getGitHub().getMyself()
                          .getRepositories()
                          .keySet();
    }

    @Override
    public String getTagUrl() throws IOException {
        return getHtmlUrl() + "/tree";
    }

    @Override
    public String getHtmlUrl() throws IOException {
        return getRepo().getHtmlUrl()
                        .toExternalForm();
    }

    @Override
    public String getCloneUrl() throws IOException {
        return getHtmlUrl() + ".git";
    }

}
