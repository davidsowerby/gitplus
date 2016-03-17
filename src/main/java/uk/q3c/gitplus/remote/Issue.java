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
public class Issue {

    private String title;
    private Set<String> labels = new HashSet<>();
    private String body;
    private int number;
    private String htmlUrl;

    public Issue(@Nonnull GHIssue ghIssue) throws IOException {
        checkNotNull(ghIssue);
        title = ghIssue.getTitle();
        ghIssue.getLabels()
               .forEach(l -> labels.add(l.getName()));
        body = ghIssue.getBody();
        number = ghIssue.getNumber();
        htmlUrl = ghIssue.getHtmlUrl()
                         .toExternalForm();
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

    public Issue number(final int number) {
        this.number = number;
        return this;
    }

    public Issue htmlUrl(final String htmlUrl) {
        this.htmlUrl = htmlUrl;
        return this;
    }
}
