package uk.q3c.gitplus.origin;

import org.kohsuke.github.GHIssue;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by David Sowerby on 08 Mar 2016
 */
public interface OriginServiceApi {

    enum Provider {GITHUB}


    String getRepoName();

    /**
     * Sets the repo name, for example, davidsowerby/krail
     *
     * @param repoName the repo name, for example, davidsowerby/krail
     */
    void setRepoName(@Nonnull String repoName);

    void setApiToken(@Nonnull String apiToken);

    boolean isIssueFixWord(String previousToken);


    /**
     * Note that this returns a {@link GHIssue}, which is GitHub specific.  If other providers - BitBucket for example - are implemented, their issues will
     * need to be mapped to {@link GHIssue} (or maybe do something smart with generics, but there would be no common interface).  The repoName is that set as
     * a property to the instance in use
     *
     * @param issueNumber the issue number to get
     * @return the issue for the given number
     */
    GHIssue getIssue(int issueNumber) throws IOException;

    /**
     * Note that this returns a {@link GHIssue}, which is GitHub specific.  If other providers - BitBucket for example - are implemented, their issues will
     * need to be mapped to {@link GHIssue} (or maybe do something smart with generics, but there would be no common interface)
     *
     * @param repoName    the repo to get the issue from
     * @param issueNumber the issue number to get
     * @return the issue for the given number
     */
    GHIssue getIssue(String repoName, int issueNumber) throws IOException;
}
