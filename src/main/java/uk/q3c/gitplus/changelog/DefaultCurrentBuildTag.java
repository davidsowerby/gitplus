package uk.q3c.gitplus.changelog;

import uk.q3c.gitplus.local.GitCommit;
import uk.q3c.gitplus.local.Tag;

import javax.annotation.Nonnull;

/**
 * A specific Tag instance used to identify the most recent build, without actually tagging Git directly
 * Created by David Sowerby on 30 Mar 2016
 */
public class DefaultCurrentBuildTag extends Tag {

    public DefaultCurrentBuildTag(@Nonnull GitCommit commit) {
        super("current build");
        this.tagType(Tag.TagType.PSEUDO)
            .releaseDate(commit.getCommitDate())
            .fullMessage("Pseudo tag on latest commit")
            .taggerIdent(commit.getCommitter())
            .commit(commit);

    }
}
