package uk.q3c.gitplus.local;

import org.eclipse.jgit.lib.PersonIdent;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by David Sowerby on 22 Mar 2016
 */
public class Tag {
    /**
     * LIGHTWEIGHT and ANNOTATED have the equivalent in Git itself, PSEUDO is used internally by GitPlus to represent the latest build, which may not yet
     * have been tagged for release
     */
    public enum TagType {
        LIGHTWEIGHT, ANNOTATED, PSEUDO
    }

    private final String tagName;
    private ZonedDateTime releaseDate;
    private ZonedDateTime commitDate;
    private PersonIdent taggerIdent;
    private String fullMessage;
    private GitCommit commit;
    private TagType tagType;

    public Tag(@Nonnull String tagName) {
        checkNotNull(tagName);
        this.tagName = tagName;
    }

    public Tag releaseDate(@Nonnull final ZonedDateTime releaseDate) {
        checkNotNull(releaseDate);
        this.releaseDate = releaseDate;
        return this;
    }

    /**
     * If true, the {@link #releaseDate} and {@link #taggerIdent} are taken from the code commit - which may not be accurate, as there is no guarantee that
     * the tag was
     * applied by the same person or the same time as the commit was made. The recommendation is to use annotated tags, but this option is retained as it
     * gives at least some information.
     *
     * @return true of the original Git tag is lightweight, false if it was an annotated tag
     */
    public TagType getTagType() {
        return tagType;
    }

    /**
     * The code commit to which this tag points
     *
     * @return The code commit to which this tag points
     */
    public GitCommit getCommit() {
        return commit;
    }

    public String getTagName() {
        return tagName;
    }

    public ZonedDateTime getReleaseDate() {
        return releaseDate;
    }

    public ZonedDateTime getCommitDate() {
        return commitDate;
    }

    public PersonIdent getTaggerIdent() {
        return taggerIdent;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public Tag commitDate(@Nonnull final ZonedDateTime commitDate) {
        checkNotNull(commitDate);
        this.commitDate = commitDate;
        return this;
    }

    public Tag taggerIdent(@Nonnull final PersonIdent taggerIdent) {
        checkNotNull(taggerIdent);
        this.taggerIdent = taggerIdent;
        return this;
    }

    public Tag fullMessage(@Nonnull final String fullMessage) {
        checkNotNull(fullMessage);
        this.fullMessage = fullMessage;
        return this;
    }

    public Tag commit(@Nonnull final GitCommit commit) {
        checkNotNull(commit);
        this.commit = commit;
        return this;
    }

    public Tag tagType(@Nonnull final TagType tagType) {
        this.tagType = tagType;
        return this;
    }

    public Tag noTagMessage() {
        fullMessage = "No tag message available";
        return this;
    }

    public boolean isPseudoTag() {
        return tagType == TagType.PSEUDO;
    }
}