package uk.q3c.gitplus.git

import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import spock.lang.Specification
import uk.q3c.gitplus.origin.OriginServiceApi

import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Limited unit testing - tried to Mock RevCommit with Spock and Mockito, both failed, probably because of the
 * static call to RawParseUtils.commitMessage in getFullMessage().  Also, getFullMessage cannot be overridden,
 * as it is final. Will have to rely on higher level testing
 *
 * Created by David Sowerby on 09 Mar 2016
 */
class VersionRecordTest extends Specification {


    VersionRecord record;
    RevCommit tagCommit = Mock(RevCommit)
    RevCommit rc1 = Mock(RevCommit)
    RevCommit rc2 = Mock(RevCommit)
    OriginServiceApi originServiceApi = Mock(OriginServiceApi)
    RevWalk walk = Mock(RevWalk)

    def setup() {
        tagCommit.getFullMessage() >> 'asdffdsfd'
        rc1.getShortMessage() >> 'short message 1'
        rc2.getShortMessage() >> 'short message 2'
        rc1.getFullMessage() >> 'full message 1'
        rc2.getFullMessage() >> 'full message 2'
    }

    def "construct with all parameters and get"() {
        given:
        final String tagName = "derib"
        LocalDateTime commitDate = LocalDateTime.of(2010, 11, 11, 12, 2)
        LocalDateTime releaseDate = LocalDateTime.of(2015, 1, 11, 12, 12)

        when:
        record = new VersionRecord(tagName, releaseDate, commitDate, tagCommit)

        then:
        record.getCommitDate().toInstant().equals(commitDate.toInstant(ZoneOffset.ofHours(0)))
        record.getReleaseDate().toInstant().equals(releaseDate.toInstant(ZoneOffset.ofHours(0)))
        record.getTagName().equals(tagName)
        record.getTagCommit().equals(tagCommit)
    }


    def "construct with no release date returns commit date as release date"() {
        given:
        final String tagName = "derib"
        LocalDateTime commitDate = LocalDateTime.of(2010, 11, 11, 12, 2)

        when:
        record = new VersionRecord(tagName, null, commitDate, tagCommit)

        then:
        record.getCommitDate().toInstant().equals(commitDate.toInstant(ZoneOffset.ofHours(0)))
        record.getReleaseDate().toInstant().equals(commitDate.toInstant(ZoneOffset.ofHours(0)))
        record.getTagName().equals(tagName)
        record.getTagCommit().equals(tagCommit)
    }

    def "parse"() {
        given:
        final String tagName = "derib"
        LocalDateTime commitDate = LocalDateTime.of(2010, 11, 11, 12, 2)
        LocalDateTime releaseDate = LocalDateTime.of(2015, 1, 11, 12, 12)
        record = new VersionRecord(tagName, releaseDate, commitDate, tagCommit)


        when:
        record.addCommit(rc1)
        record.addCommit(rc2)

        then:
        record.getRevCommits().size() == 2
    }
}
