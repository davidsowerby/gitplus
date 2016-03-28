package uk.q3c.gitplus.remote;

import org.kohsuke.github.GHIssue;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A non-remote specific Issue - it does not carry everytihng a 'native' issue does, but could be extended if need arises
 * <p>
 * Created by David Sowerby on 22 Mar 2016
 */
public class Issue implements Comparable<Issue> {

    private String title;
    private Set<String> labels = new HashSet<>();
    private String body;
    private int number;
    private String htmlUrl;
    private boolean pullRequest;

    public Issue(@Nonnull GHIssue ghIssue) throws IOException {
        checkNotNull(ghIssue);
        title = ghIssue.getTitle();
        ghIssue.getLabels()
               .forEach(l -> labels.add(l.getName()));
        body = ghIssue.getBody();
        number = ghIssue.getNumber();
        htmlUrl = ghIssue.getHtmlUrl()
                         .toExternalForm();
        pullRequest = ghIssue.isPullRequest();
    }

    public Issue(int number) {
        this.number = number;
    }

    public Issue title(final String title) {
        this.title = title;
        return this;
    }

    public int getNumber() {
        return number;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public Set<String> getLabels() {
        return labels;
    }


    public String getTitle() {
        return title;
    }


    public String getBody() {
        return body;
    }

    public boolean hasLabel(String candidate) {
        return labels.contains(candidate);
    }


    public Issue labels(final Set<String> labels) {
        this.labels = labels;
        return this;
    }

    public Issue body(final String body) {
        this.body = body;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Issue issue = (Issue) o;

        if (number != issue.number) {
            return false;
        }
        return htmlUrl.equals(issue.htmlUrl);

    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + htmlUrl.hashCode();
        return result;
    }


    public Issue htmlUrl(final String htmlUrl) {
        this.htmlUrl = htmlUrl;
        return this;
    }

    public Issue pullRequest(boolean pullRequest) {
        this.pullRequest = pullRequest;
        return this;
    }

    @Override
    public int compareTo(Issue o) {
        if (o == null) {
            return -1;
        }
        int h = htmlUrl.compareTo(o.htmlUrl);
        if (h != 0) {
            return h;
        }
        return this.number - o.number;
    }

    public boolean isPullRequest() {
        return pullRequest;
    }
}
