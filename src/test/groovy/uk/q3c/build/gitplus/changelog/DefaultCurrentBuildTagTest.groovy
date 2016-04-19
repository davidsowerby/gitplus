package uk.q3c.build.gitplus.changelog

import org.eclipse.jgit.lib.PersonIdent
import spock.lang.Specification
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.Tag

import java.time.ZonedDateTime

/**
 * Created by David Sowerby on 30 Mar 2016
 */
class DefaultCurrentBuildTagTest extends Specification {

    def "construct"() {
        given:
        ZonedDateTime now = ZonedDateTime.now()
        PersonIdent committer = Mock(PersonIdent)
        GitCommit commit = Mock(GitCommit)
        commit.getCommitter() >> committer
        commit.getCommitDate() >> now

        when:
        DefaultCurrentBuildTag tag = new DefaultCurrentBuildTag(commit)

        then:
        tag.getReleaseDate().equals(now)
        tag.getCommit().equals(commit)
        tag.getFullMessage().equals('Pseudo tag on latest commit')
        tag.getTaggerIdent().equals(committer)
        tag.getTagType() == Tag.TagType.PSEUDO
    }
}
