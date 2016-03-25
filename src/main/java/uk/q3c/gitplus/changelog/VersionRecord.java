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
    private List<GitCommit> commits;
    private Map<String, Set<Issue>> fixesByGroup;
    private Map<String, String> labelLookup;

    public VersionRecord(@Nonnull Tag tag, @Nonnull ChangeLogConfiguration changeLogConfiguration) {
        checkNotNull(tag);
        checkNotNull(changeLogConfiguration);
        this.tag = tag;
        createLabelLookup(changeLogConfiguration.getLabelGroups());
        commits = new ArrayList<>();
        fixesByGroup = new HashMap<>();

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

    public List<String> labelGroups() {
        return new ArrayList<>(fixesByGroup.keySet());
    }

    public Set<Issue> issuesInGroup(String groupName) {
        return fixesByGroup.get(groupName);
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

    public void addCommit(GitCommit commit) {
        commits.add(commit);
    }

    /**
     * Expands commit and issue information to make it ready for output to Velocity
     *
     * @param gitRemote the service hosting issues etc (for example GitHub)
     *                  name
     */
    public void parse(@Nonnull GitRemote gitRemote) throws IOException {
        checkNotNull(gitRemote);

        commits.forEach(c -> {
                    c.extractIssueReferences(gitRemote);
                    c.getIssueReferences()
                     .forEach(issue -> issue.getLabels()
                                            .forEach(l -> {
                                                String group = labelLookup.get(l);

                                                //if label in a group
                                                if (group != null) {
                                                    //create an entry set if none already
                                                    if (!fixesByGroup.containsKey(group)) {
                                                        fixesByGroup.put(group, new TreeSet<>());
                                                    }
                                                    fixesByGroup.get(group)
                                                                .add(issue);
                                                }
                                            }));
                }
        );


    }

    public PersonIdent getPersonIdent() {
        return tag.getTaggerIdent();
    }
}
