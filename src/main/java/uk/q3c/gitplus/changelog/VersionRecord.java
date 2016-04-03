package uk.q3c.gitplus.changelog;

import org.eclipse.jgit.lib.PersonIdent;
import org.slf4j.Logger;
import uk.q3c.gitplus.local.GitCommit;
import uk.q3c.gitplus.local.Tag;
import uk.q3c.gitplus.remote.GitRemote;
import uk.q3c.gitplus.remote.Issue;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by David Sowerby on 07 Mar 2016
 */
public class VersionRecord {
    private static Logger log = getLogger(VersionRecord.class);
    private final Tag tag;
    private final String pullRequestsTitle;
    private final ChangeLogConfiguration changeLogConfiguration;
    private List<GitCommit> commits;
    private List<GitCommit> excludedCommits;
    private Map<String, Set<Issue>> fixesByGroup;
    private Map<String, String> labelLookup;
    private Set<Issue> pullRequests;
    public VersionRecord(@Nonnull Tag tag, @Nonnull ChangeLogConfiguration changeLogConfiguration) {
        checkNotNull(tag);
        checkNotNull(changeLogConfiguration);
        this.tag = tag;
        createLabelLookup(changeLogConfiguration.getLabelGroups());
        pullRequests = new TreeSet<>();
        commits = new ArrayList<>();
        excludedCommits = new ArrayList<>();
        fixesByGroup = new LinkedHashMap<>();
        changeLogConfiguration.getLabelGroups()
                              .forEach((group, labels) -> fixesByGroup.put(group, new TreeSet<>()));
        this.changeLogConfiguration = changeLogConfiguration;
        pullRequestsTitle = changeLogConfiguration.getPullRequestTitle();

    }

    public List<GitCommit> getExcludedCommits() {
        return excludedCommits;
    }

    public Tag getTag() {
        return tag;
    }

    public Map<String, String> getLabelLookup() {
        return labelLookup;
    }

    private void createLabelLookup(Map<String, Set<String>> labelGroups) {
        labelLookup = new HashMap<>();
        labelGroups.forEach((k, v) -> {
            v.forEach(l ->
                    labelLookup.put(l, k)
            );
        });
    }

    public Map<String, Set<Issue>> getFixesByGroup() {
        return fixesByGroup;
    }

    public List<GitCommit> getCommits() {
        return commits;
    }

    public String getTagName() {
        return tag.getTagName();
    }

    public ZonedDateTime getReleaseDate() {
        if (tag.getReleaseDate() == null) {
            return getCommitDate();
        }
        return tag.getReleaseDate();
    }

    /**
     * {@link #getReleaseDate} converted to {@link Date} (primarily for Velocity)
     *
     * @return {@link #getReleaseDate} converted to {@link Date} (primarily for Velocity)
     */
    public Date getReleaseDateAsDate() {
        return Date.from(getReleaseDate().toInstant());
    }

    /**
     * {@link #getCommitDate} converted to {@link Date} (primarily for Velocity)
     *
     * @return {@link #getCommitDate} converted to {@link Date} (primarily for Velocity)
     */
    public Date getCommitDateAsDate() {
        return Date.from(getCommitDate().toInstant());
    }

    public ZonedDateTime getCommitDate() {
        return tag.getCommitDate();
    }

    public GitCommit getTagCommit() {
        return tag.getCommit();
    }

    public void addCommit(@Nonnull GitCommit commit) {
        checkNotNull(commit);
        if (commit.excludedFromChangeLog(changeLogConfiguration)) {
            excludedCommits.add(commit);
        } else {
            commits.add(commit);
        }
    }

    /**
     * Expands commit and issue information to make it ready for output to Velocity
     *
     * @param gitRemote the service hosting issues etc (for example GitHub)
     *                  name
     */
    public void parse(@Nonnull GitRemote gitRemote) throws IOException {
        checkNotNull(gitRemote);
        for (GitCommit c : commits) {
            if (!c.excludedFromChangeLog(changeLogConfiguration)) {
                c.extractIssueReferences(gitRemote);
                c.getIssueReferences()
                 .forEach(issue -> {
                     if (issue.isPullRequest()) {
                         pullRequests.add(issue);
                     } else {
                         mapIssueToGroups(issue);
                     }
                 });
            }
        }
        mergePullRequests();
        removeEmptyGroups();
    }

    /**
     * Using the labels on an issue, attach the issue to any group for which it has a label
     */
    private void mapIssueToGroups(Issue issue) {
        issue.getLabels()
             .forEach(l -> {
                 String group = labelLookup.get(l);
                 //if label in a group, add it, otherwise ignore
                 if (group != null) {
                     fixesByGroup.get(group)
                                 .add(issue);
                 }
             });
    }

    /**
     * If pull requests are required in the output merge them in to the label groups
     */
    private void mergePullRequests() {
        if (fixesByGroup.containsKey(pullRequestsTitle)) {
            fixesByGroup.put(pullRequestsTitle, pullRequests);
        }
    }

    /**
     * clear out any empty groups, so we don't just get headings with no entries
     */
    private void removeEmptyGroups() {
        List<String> toRemove = new ArrayList<>();
        fixesByGroup.forEach((group, issues) -> {
            if (issues.isEmpty()) {
                toRemove.add(group);
            }
        });
        toRemove.forEach(fixesByGroup::remove);
    }

    public PersonIdent getPersonIdent() {
        return tag.getTaggerIdent();
    }

    public Set<Issue> getPullRequests() {
        return pullRequests;
    }

    public boolean hasCommits() {
        return !commits.isEmpty();
    }
}
