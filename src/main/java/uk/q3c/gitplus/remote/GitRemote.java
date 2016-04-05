package uk.q3c.gitplus.remote;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.kohsuke.github.GHIssue;

import javax.annotation.Nonnull;
import java.io.IOException;
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
    Issue getIssue(int issueNumber);

    /**
     * Note that this returns a {@link GHIssue}, which is GitHub specific.  If other providers - BitBucket for example - are implemented, their issues will
     * need to be mapped to {@link GHIssue} (or maybe do something smart with generics, but there would be no common interface).  If {@code repoName} is null
     * or empty the current repo is assumed (which makes it the same as calling {@link #getIssue(int)}
     *
     * @param repoName    the repo to get the issue from
     * @param issueNumber the issue number to get
     * @return the issue for the given number
     * @throws GitRemoteException if the issue cannot be retrieved for any reason
     */
    Issue getIssue(String repoName, int issueNumber);

    CredentialsProvider getCredentialsProvider();

    GitHubRemote.Status apiStatus();


    GHIssue createIssue(@Nonnull String issueTitle, @Nonnull String body, @Nonnull String label) throws
            IOException;

    void createRepo() throws IOException;

    void deleteRepo() throws IOException;

    String getRepoName();

    /**
     * Lists the names of repositories belonging to this user
     *
     * @return the names of repositories belonging to this user
     * @throws IOException
     */
    Set<String> listRepositoryNames() throws IOException;

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
}
