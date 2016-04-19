package uk.q3c.build.gitplus.changelog;

import uk.q3c.build.gitplus.local.GitCommit;
import uk.q3c.build.gitplus.local.Tag;

import javax.annotation.Nonnull;

/**
 * A pseudo Tag instance used to identify the most recent build, without actually tagging Git directly
 * <p>
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

    /**
     * When used with the tag url, forces the Git tree to look at the develop branch, rather than the default version number from the tag name
     */
    @Override
    public String getUrlSegment() {
        return "develop";
    }
}
