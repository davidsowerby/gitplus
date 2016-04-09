package uk.q3c.gitplus.remote;

import com.google.common.collect.ImmutableList;
import com.jcabi.github.*;
import com.jcabi.http.Request;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import uk.q3c.gitplus.gitplus.GitPlusConfiguration;

import javax.annotation.Nonnull;
import javax.json.JsonReader;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.q3c.gitplus.remote.GitRemote.TokenScope.*;

/**
 * Created by David Sowerby on 12 Feb 2016
 */
public class GitHubRemote implements GitRemote {
    public enum Status {GREEN, YELLOW, RED}

    public static final String STATUS_API_URL = "https://status.github.com/api/status.json";
    private static final ImmutableList<String> fixWords = ImmutableList.of("fix", "fixes", "fixed", "resolve", "resolves", "resolved", "close", "closes",
            "closed");
    private static Logger log = getLogger(GitHubRemote.class);
    private final RemoteRequest remoteRequest;
    private final GitHubProvider gitHubProvider;
    private Github gitHub;
    private GitPlusConfiguration configuration;
    private TokenScope currentTokenScope;

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
        return word != null && fixWords.contains(word.toLowerCase());
    }


    @Override
    public GPIssue getIssue(int issueNumber) {
        return getIssue(configuration.getRemoteRepoUser(), configuration.getRemoteRepoName(), issueNumber);
    }

    @Override
    public GPIssue getIssue(@Nonnull String remoteRepoUser, @Nonnull String remoteRepoName, int issueNumber) {
        checkNotNull(remoteRepoName);
        checkNotNull(remoteRepoUser);
        log.debug("Retrieving issue {} from {}", issueNumber, remoteRepoName);
        try {
            Repo repo = getGitHub(RESTRICTED).repos()
                                             .get(new Coordinates.Simple(remoteRepoUser, remoteRepoName));
            return new GPIssue(repo.issues()
                                   .get(issueNumber));
        } catch (Exception e) {
            throw new GitRemoteException("Unable to retrieve issue " + issueNumber + " from " + remoteRepoName, e);
        }
    }

    @Override
    public CredentialsProvider getCredentialsProvider() {
        try {
            String token = configuration.getApiTokenRestricted();
            return new UsernamePasswordCredentialsProvider(token, "");
        } catch (Exception e) {
            throw new GitRemoteException("An api token is required in order to enable credentials", e);
        }
    }

    @Override
    public Status apiStatus() {

        try {
            String token = configuration.getApiTokenRestricted();

            JsonReader response = remoteRequest.request(Request.GET, STATUS_API_URL, token);
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
        } catch (Exception e) {
            log.error("Unable to retrieve status from online service", e);
            return Status.RED;
        }
    }


    @Override
    public GPIssue createIssue(@Nonnull String issueTitle, @Nonnull String body, @Nonnull String... labels) throws
            IOException {
        checkNotNull(issueTitle);
        checkNotNull(body);
        checkNotNull(labels);
        final Issue issue = getRepo(RESTRICTED).issues()
                                               .create(issueTitle, body);

        final Issue.Smart jsIssue = new Issue.Smart(issue);
        jsIssue.assign(configuration.getRemoteRepoUser());
        jsIssue.labels()
               .add(ImmutableList.copyOf(labels));
        return new GPIssue(jsIssue);
    }

    private Github getGitHub(TokenScope tokenScope) throws IOException {
        if (gitHub == null || currentTokenScope != tokenScope) {
            this.gitHub = gitHubProvider.get(configuration, tokenScope);
            currentTokenScope = tokenScope;
        }
        return gitHub;
    }


    public Repo getRepo(TokenScope tokenScope) throws IOException {
        Repos repos = getGitHub(tokenScope).repos();
        return repos.get(new Coordinates.Simple(configuration.getRemoteRepoUser(), configuration.getRemoteRepoName()));
    }

    /**
     * Creates the remote repo from the information in {@link #configuration}.  Note that there is no setting
     * for any option to control creation of a wiki - it is always created, even if {@link #configuration} has useWiki set to false.  That said, you still
     * wll not be able to access the wiki via the API until you manually create the first page.
     */
    @Override
    public void createRepo() {
        try {
            Repos.RepoCreate settings = new Repos.RepoCreate(configuration.getRemoteRepoName(), !configuration.isPublicProject());
            settings.withDescription(configuration.getProjectDescription())
                    .withHomepage(configuration.getProjectHomePage());
            getGitHub(CREATE_REPO).repos()
                                  .create(settings);
            if (configuration.isMergeIssueLabels()) {
                mergeLabels();
            }
        } catch (Exception e) {
            throw new GitRemoteException("Unable to create Repo", e);
        }
    }


    @Override
    public void deleteRepo() {
        try {
            if (configuration.deleteRepoApproved()) {
                getGitHub(DELETE_REPO).repos()
                                      .remove(new Coordinates.Simple(configuration.getRemoteRepoUser(), configuration.getRemoteRepoName()));
            } else {
                throw new GitRemoteException("Repo deletion not confirmed");
            }
        } catch (Exception e) {
            throw new GitRemoteException("Unable to delete remote repo", e);
        }
    }

    @Override
    public String getRepoName() {
        return configuration.getRemoteRepoName();
    }

    @Override
    public String getTagUrl() throws IOException {
        return getHtmlUrl() + "/tree";
    }

    @Override
    public String getHtmlUrl() throws IOException {
        return configuration.getRemoteRepoHtmlUrl();
    }

    @Override
    public String getCloneUrl() throws IOException {
        return getHtmlUrl() + ".git";
    }

    @Override
    public Set<String> listRepositoryNames() throws IOException {
        String qualifier = "user:" + getRepo(RESTRICTED).coordinates()
                                                        .user();
        Set<String> repoNames = new TreeSet<>();
        final Iterable<Repo> repos = getGitHub(RESTRICTED).search()
                                                          .repos(qualifier, "coordinates", Search.Order.ASC);
        repos.iterator()
             .forEachRemaining(r -> repoNames.add(r.coordinates()
                                                   .repo()));
        return repoNames;
    }

    @Override
    public void mergeLabels() throws IOException {
        mergeLabels(configuration.getIssueLabels());
    }


    @Override
    public Map<String, String> mergeLabels(Map<String, String> labelsToMerge) throws IOException {
        final Labels labels = getRepo(RESTRICTED).labels();
        final Iterable<Label> iterate = labels.iterate();
        final Iterator<Label> iterator = iterate.iterator();

        //copy of map, we need to remove entries to keep track, and we may have been passed an ImmutableMap
        Map<String, String> lblsToMerge = new TreeMap<>(labelsToMerge);

        //take existing labels first, update any colours which have changed, and remove labels that are not in the 'labelsToMerge'
        List<Label> existingToRemove = new ArrayList<>();
        while (iterator.hasNext()) {
            Label currentLabel = iterator.next();
            String currentName = currentLabel.name();
            //if this repo label is in new set as well, update the colour if, but only if not already the same (save unnecessary API calls)
            if (labelsToMerge.containsKey(currentName)) {
                String mergeColour = labelsToMerge.get(currentName);
                Label.Smart smLabel = new Label.Smart(currentLabel);
                if (!smLabel.color()
                            .equals(mergeColour)) {
                    smLabel.color(mergeColour);// this does the update (patch)
                }
                //processed, remove from merge set
                lblsToMerge.remove(currentName);
            } else {
                // it is not in the new set, note it for removal
                existingToRemove.add(currentLabel);
            }
        }

        //remove the unwanted
        for (Label label : existingToRemove) {
            log.debug("deleting label '{}", label.name());
            labels.delete(label.name());
        }

        //existing set is updated except for any new ones that need to be added
        //lblsToMerge now contains only those which have not been processed
        for (Map.Entry<String, String> entry : lblsToMerge.entrySet()) {
            try {
                labels.create(entry.getKey(), entry.getValue());
            } catch (IOException ioe) {
                log.warn("Failed to add new label '{}'", entry.getKey(), ioe);
            }
        }

        // do final check to make sure the two sets are the same, if different throw exception
        Map<String, String> checkMap = getLabelsAsMap();

        if (!checkMap.equals(labelsToMerge)) {
            throw new GitRemoteException("Labels did not merge correctly");
        }
        return checkMap;
    }


    @Override
    public Map<String, String> getLabelsAsMap() throws IOException {
        final Labels labels = getRepo(RESTRICTED).labels();
        Map<String, String> map = new TreeMap<>();
        for (Label label : labels.iterate()) {
            Label.Smart smLabel = new Label.Smart(label);
            map.put(smLabel.name(), smLabel.color());
        }
        return map;
    }


}
