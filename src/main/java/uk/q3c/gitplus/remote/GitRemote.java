package uk.q3c.gitplus.remote;

import org.eclipse.jgit.transport.CredentialsProvider;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Set;

/**
 * Created by David Sowerby on 08 Mar 2016
 */
public interface GitRemote<ISSUE, LABEL> {

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
     * @return
     */
    String getHtmlUrl() throws IOException;

    String getCloneUrl() throws IOException;

    Set<String> listRepositoryNames() throws IOException;
}
