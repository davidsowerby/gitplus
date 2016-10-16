package uk.q3c.build.gitplus.local

import org.eclipse.jgit.lib.PersonIdent
import uk.q3c.build.gitplus.local.Tag.TagType
import java.time.ZonedDateTime

/**
 * @param tagType If [TagType.LIGHTWEIGHT], the [releaseDate] and [taggerIdent] are taken from the code commit - which may not be accurate,
 * as there is no guarantee that the tag was applied by the same person or the same time as the commit was made. The recommendation is to
 * use annotated tags, but this option is retained as it gives at least some information.
 * @param commit The code commit to which this tag points
 * Created by David Sowerby on 22 Mar 2016
 */
open class Tag(val tagName: String, val releaseDate: ZonedDateTime, val commitDate: ZonedDateTime, val taggerIdent: PersonIdent, val fullMessage: String, val commit: GitCommit, val tagType: TagType) {
    /**
     * LIGHTWEIGHT and ANNOTATED have the equivalent in Git itself, PSEUDO is used internally by [DefaultCurrentBuildTag] to represent the latest build, which may not yet
     * have been tagged for release
     */
    enum class TagType {
        LIGHTWEIGHT, ANNOTATED, PSEUDO
    }

    open val urlSegment = tagName
}