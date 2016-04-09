package uk.q3c.gitplus.remote;

import org.eclipse.jgit.transport.CredentialsProvider;
import uk.q3c.gitplus.gitplus.GitPlusConfiguration;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by David Sowerby on 08 Mar 2016
 */
public interface GitRemote {

    enum ServiceProvider {GITHUB, BITBUCKET}

    enum TokenScope {RESTRICTED, CREATE_REPO, DELETE_REPO}

    boolean isIssueFixWord(String previousToken);


    /**
     * Returns a representation of the issue
     *
     * @param issueNumber the issue number to get
     * @return the issue for the given number
     * @throws GitRemoteException if the issue cannot be retrieved for any reason
     */
    GPIssue getIssue(int issueNumber);

    /**
     * Note that this returns a {@link GPIssue}, which contains only a subset of the information available - this is intended for use with the changelog.  You
     * can access the full issue implementation by accessing the underlying repository.  If {@code repoName} is null or empty the current repo is assumed
     * (which makes it the same as calling {@link #getIssue(int)}
     *
     * @param remoteRepoUser user name for the repo to get the issue from
     * @param remoteRepoName repo name for the repo to get the issue from
     * @param issueNumber    the issue number to get
     * @return the issue for the given number
     * @throws GitRemoteException if the issue cannot be retrieved for any reason
     */
    GPIssue getIssue(@Nonnull String remoteRepoUser, @Nonnull String remoteRepoName, int issueNumber);

    CredentialsProvider getCredentialsProvider();

    GitHubRemote.Status apiStatus();


    GPIssue createIssue(@Nonnull String issueTitle, @Nonnull String body, @Nonnull String... labels) throws
            IOException;

    void createRepo() throws IOException;

    void deleteRepo() throws IOException;

    String getRepoName();

    /**
     * Returns the base url for tags
     *
     * @return the base url for tags
     */

    String getTagUrl() throws IOException;

    /**
     * Return the http Url for this repo (not the Api Url)
     *
     * @return the http Url for this repo (not the Api Url)
     */
    String getHtmlUrl() throws IOException;

    String getCloneUrl() throws IOException;

    Set<String> listRepositoryNames() throws IOException;

    /**
     * Merges the issue labels of the current repository with those defined by {@link GitPlusConfiguration},  The equivalent of calling
     * {@link #mergeLabels(Map)}
     * with {@link GitPlusConfiguration#getIssueLabels()}
     */
    void mergeLabels() throws IOException;

    /**
     * Merges the issue labels of the current repository with those defined by {@link GitPlusConfiguration}.  'Merge' means:<ol>
     * <li>if a label in the current repository has the same name as one in {@code labelsToMerge} the current label colour is updated to that in {@code
     * labelsToMerge}.  This ensures that a which is being retained is not removed from any issues
     * exists </li>
     * <li>If a label does not exist in the current repository, it is added</li>
     * <li>If a label exists in the current repository, but not in {@code labelsToMerge}, it is removed. (And will therefore be removed from any issues it is
     * attached to</li>
     * </ol>
     */


    Map<String, String> mergeLabels(Map<String, String> labelsToMerge) throws IOException;

    /**
     * Returns issue labels as a map where (K,V) = (label name, label colour)
     *
     * @return issue labels as a map where (K,V) = (label name, label colour)
     * @throws IOException
     */
    Map<String, String> getLabelsAsMap() throws IOException;
}
