package uk.q3c.gitplus.changelog

import com.google.common.collect.ImmutableSet
import org.eclipse.jgit.lib.PersonIdent
import spock.lang.Specification
import uk.q3c.gitplus.local.GitCommit
import uk.q3c.gitplus.local.Tag
import uk.q3c.gitplus.remote.GitRemote
import uk.q3c.gitplus.remote.Issue

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
/**
 * Limited unit testing - tried to Mock RevCommit with Spock and Mockito, both failed, probably because of the
 * static call to RawParseUtils.commitMessage in getFullMessage().  Also, getFullMessage cannot be overridden,
 * as it is final. Will have to rely on higher level testing
 *
 * Created by David Sowerby on 09 Mar 2016
 */
class VersionRecordTest extends Specification {


    VersionRecord record;
    GitCommit rc1 = Mock(GitCommit)
    PersonIdent personIdent = Mock(PersonIdent)
    final String fullMessage = 'full message'
    GitRemote gitRemote = Mock(GitRemote)

    def setup() {

    }


    def "construct builds labelLookup"() {
        given:
        final String tagName = "0.1"
        ZonedDateTime commitDate = ZonedDateTime.of(LocalDateTime.of(2010, 11, 11, 12, 2), ZoneId.systemDefault())
        ZonedDateTime releaseDate = ZonedDateTime.of(LocalDateTime.of(2015, 1, 11, 12, 12), ZoneId.systemDefault())
        Tag tag = new Tag(tagName)
        ChangeLogConfiguration changeLogConfiguration = Mock(ChangeLogConfiguration)
        changeLogConfiguration.getLabelGroups() >> ChangeLogConfiguration.defaultLabelGroups

        when:
        record = new VersionRecord(tag, changeLogConfiguration)


        then:
        Map<String, String> lookup = record.getLabelLookup()
        lookup.get('bug').equals('Fixes')
        lookup.get('enhancement').equals('Enhancements')
        lookup.get('performance').equals('Enhancements')
        lookup.get('testing').equals('Quality')
        lookup.get('task').equals('Tasks')

    }

    def "construct with all parameters and get"() {
        given:
        final String tagName = "0.1"
        ZonedDateTime commitDate = ZonedDateTime.of(LocalDateTime.of(2010, 11, 11, 12, 2), ZoneId.of("Z"))
        ZonedDateTime releaseDate = ZonedDateTime.of(LocalDateTime.of(2015, 1, 11, 12, 12), ZoneId.of("Z"))
        Tag tag = new Tag(tagName)
                .commitDate(commitDate)
                .releaseDate(releaseDate)
                .commit(rc1)
                .taggerIdent(personIdent)
        ChangeLogConfiguration changeLogConfiguration = Mock(ChangeLogConfiguration)
        changeLogConfiguration.getLabelGroups() >> ChangeLogConfiguration.defaultLabelGroups
        GitCommit commit1 = Mock(GitCommit)

        when:
        record = new VersionRecord(tag, changeLogConfiguration)
        record.addCommit(commit1)

        then:
        record.getCommitDate().equals(commitDate)
        record.getReleaseDate().equals(releaseDate)
        record.getTagName().equals(tagName)
        record.getTagCommit().equals(rc1)
        record.getPersonIdent() == personIdent
        record.getReleaseDateAsDate().toInstant().equals(releaseDate.toInstant())
        record.commitDateAsDate.toInstant().equals(commitDate.toInstant())
        record.getCommits().size() == 1
        record.getCommits().contains(commit1)
    }


    def "construct with no release date returns commit date as release date"() {
        given:
        final String tagName = "derib"
        ZonedDateTime commitDate = ZonedDateTime.of(LocalDateTime.of(2010, 11, 11, 12, 2), ZoneId.systemDefault())
        Tag tag = new Tag(tagName)
                .commitDate(commitDate)
                .commit(rc1)
                .taggerIdent(personIdent)
        ChangeLogConfiguration changeLogConfiguration = Mock(ChangeLogConfiguration)
        changeLogConfiguration.getLabelGroups() >> ChangeLogConfiguration.defaultLabelGroups

        when:
        record = new VersionRecord(tag, changeLogConfiguration)

        then:
        record.getCommitDate().equals(commitDate)
        record.getReleaseDate().equals(commitDate)
        record.getTagName().equals(tagName)
        record.getTagCommit().equals(rc1)
    }

    def "parse"() {
        given:
        final String tagName = "0.1"
        ZonedDateTime commitDate = ZonedDateTime.of(LocalDateTime.of(2010, 11, 11, 12, 2), ZoneId.systemDefault())
        ZonedDateTime releaseDate = ZonedDateTime.of(LocalDateTime.of(2015, 1, 11, 12, 12), ZoneId.systemDefault())
        ChangeLogConfiguration changeLogConfiguration = Mock(ChangeLogConfiguration)
        changeLogConfiguration.getLabelGroups() >> ChangeLogConfiguration.defaultLabelGroups
        Tag tag = new Tag(tagName)
                .commitDate(commitDate)
                .releaseDate(releaseDate)
                .commit(rc1)
                .taggerIdent(personIdent)
        final String msgNormalShort = 'Fix #1 Removed redundant call'
        Issue issue1 = newIssue(1, 'Making unnecessary calls')
        record = new VersionRecord(tag, changeLogConfiguration)
        record.addCommit(new GitCommit(msgNormalShort))
        gitRemote.isIssueFixWord('Fix') >> true

        when:
        record.parse(gitRemote)
        Map<String, Set<Issue>> fixes = record.getFixesByGroup()

        then:
        1 * gitRemote.getIssue('', 1) >> issue1
        fixes.size() == 1


    }


    private Issue newIssue(int number, String title) {
        return new Issue(number).title(title).htmlUrl('https:/github.com/davidsowerby/dummy/issues/1').labels(ImmutableSet.of('bug'))
    }
}
