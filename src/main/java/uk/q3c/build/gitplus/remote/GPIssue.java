package uk.q3c.build.gitplus.remote;

import com.jcabi.github.Issue;
import com.jcabi.github.IssueLabels;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A generic Issue (one that is not specific to a remote provider) - it does not carry everything the 'native' issue does, just what is required for the
 * GitPlus build and changelog functions.
 * <p>
 * Created by David Sowerby on 22 Mar 2016
 */
public class GPIssue implements Comparable<GPIssue> {

    private String title;
    private Set<String> labels = new HashSet<>();
    private String body;
    private int number;
    private String htmlUrl;
    private boolean pullRequest;

    public GPIssue(@Nonnull Issue jIssue) throws IOException {
        checkNotNull(jIssue);
        Issue.Smart jsIssue = new Issue.Smart(jIssue);

        title = jsIssue.title();
        IssueLabels.Smart jLabels = new IssueLabels.Smart(jsIssue.labels());
        jLabels.iterate()
               .forEach(l -> this.labels.add(l.name()));
        body = jsIssue.body();
        number = jsIssue.number();
        htmlUrl = jsIssue.htmlUrl()
                         .toExternalForm();
        pullRequest = jsIssue.isPull();
    }

    public GPIssue(int number) {
        this.number = number;
    }

    public GPIssue title(final String title) {
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


    public GPIssue labels(final Set<String> labels) {
        this.labels = labels;
        return this;
    }

    public GPIssue body(final String body) {
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

        GPIssue GPIssue = (GPIssue) o;

        if (number != GPIssue.number) {
            return false;
        }
        return htmlUrl.equals(GPIssue.htmlUrl);

    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + htmlUrl.hashCode();
        return result;
    }


    public GPIssue htmlUrl(final String htmlUrl) {
        this.htmlUrl = htmlUrl;
        return this;
    }

    public GPIssue pullRequest(boolean pullRequest) {
        this.pullRequest = pullRequest;
        return this;
    }

    @Override
    public int compareTo(GPIssue o) {
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
