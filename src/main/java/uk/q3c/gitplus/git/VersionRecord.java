package uk.q3c.gitplus.git;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.kohsuke.github.GHIssue;
import org.slf4j.Logger;
import uk.q3c.gitplus.origin.OriginServiceApi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by David Sowerby on 07 Mar 2016
 */
public class VersionRecord {
    private static Logger log = getLogger(VersionRecord.class);
    private final String tagName;
    private final LocalDateTime releaseDate;
    private final LocalDateTime commitDate;
    private final RevCommit tagCommit;
    private List<GitCommit> commits;
    private List<RevCommit> revCommits;
    private Map<String, GHIssue> fixes;
    private Map<String, Set<String>> labelGroupMap;

    public VersionRecord(@Nonnull String tagName, @Nullable LocalDateTime releaseDate, @Nonnull LocalDateTime commitDate, @Nonnull RevCommit tagCommit) {
        fixes = new LinkedHashMap<>();
        labelGroupMap = defaultLabelGroupMap();
        checkNotNull(tagName);
        checkNotNull(commitDate);
        checkNotNull(tagCommit);
        commits = new ArrayList<>();
        revCommits = new ArrayList<>();
        this.tagName = tagName;
        this.releaseDate = releaseDate == null ? commitDate : releaseDate;
        this.commitDate = commitDate;
        this.tagCommit = tagCommit;

    }

    private Map<String, Set<String>> defaultLabelGroupMap() {
        LinkedHashMap<String, Set<String>> map = new LinkedHashMap<String, Set<String>>();
        map.put("Fixes", ImmutableSet.of("bug"));
        map.put("Tasks", ImmutableSet.of("task"));
        map.put("Enhancements", ImmutableSet.of("enhancement", "performance"));
        map.put("Documentation", ImmutableSet.of("documentation"));
        map.put("Other", ImmutableSet.of("testing", "wontfix"));
        return map;
    }

    public List<RevCommit> getRevCommits() {
        return revCommits;
    }

    public List<GitCommit> getCommits() {
        return commits;
    }

    public String getTagName() {
        return tagName;
    }

    public Date getReleaseDate() {
        Instant instant = releaseDate.atZone(ZoneId.systemDefault())
                                     .toInstant();
        return Date.from(instant);
    }

    public Date getCommitDate() {
        Instant instant = commitDate.atZone(ZoneId.systemDefault())
                                    .toInstant();
        return Date.from(instant);
    }

    public RevCommit getTagCommit() {
        return tagCommit;
    }

    public void addCommit(RevCommit revCommit) {
        revCommits.add(revCommit);
    }

    /**
     * Expands commit and issue information to make it ready for output to Velocity
     *
     * @param walk             JGit commit tree walker
     * @param originServiceApi the service hosting issues etc (for example GitHub)
     *                         name
     */
    public void parse(@Nonnull RevWalk walk, @Nonnull OriginServiceApi originServiceApi) throws IOException {
        checkNotNull(walk);
        checkNotNull(originServiceApi);

        extractCommits(walk);
        commits.forEach(c ->
                c.extractIssueReferences(originServiceApi));


    }

    private void extractCommits(RevWalk walk) {
        for (RevCommit rc : revCommits) {
            try {
                walk.parseBody(rc);
                GitCommit gitCommit = new GitCommit(rc);
                commits.add(gitCommit);
            } catch (IOException e) {
                commits.add(new GitCommit("Unable to Read"));
                log.error("Unable to read commit message for commit " + rc.getId(), e);
            }
        }
    }
}
