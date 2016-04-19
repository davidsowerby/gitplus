package uk.q3c.build.gitplus.changelog

import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.revwalk.RevCommit
import spock.lang.Specification
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.remote.GPIssue
import uk.q3c.build.gitplus.remote.GitRemote

/**
 * Created by David Sowerby on 09 Mar 2016
 */
class GitCommitTest extends Specification {

    GitRemote remote = Mock(GitRemote)
    GPIssue issue1 = Mock(GPIssue)
    GPIssue issue2 = Mock(GPIssue)
    GPIssue issue3 = Mock(GPIssue)
    GPIssue issue18 = Mock(GPIssue)
    RevCommit revCommit = Mock(RevCommit)
    final String fullMessage = 'full message'
    final String USER = 'davidsowerby'

    def setup() {
        String repoName = 'scratch'
        String otherRepoName = 'other'
        String issuesUrl = "https://github.com/davidsowerby/scratch/issues"
        String otherIssuesUrl = "https://api.github.com/davidsowerby/other/issues"
        remote.getRepoName() >> repoName
        remote.getIssue(USER, repoName, 1) >> issue1
        remote.getIssue(1) >> issue1
        remote.getIssue(USER, repoName, 2) >> issue2
        remote.getIssue(2) >> issue2
        remote.getIssue(USER, repoName, 3) >> issue3
        remote.getIssue(3) >> issue3
        remote.getIssue(USER, repoName, 99) >> { throw (new IOException()) }
        remote.getIssue(99) >> { throw (new IOException()) }

        remote.getIssue(USER, otherRepoName, 18) >> issue18

        remote.isIssueFixWord('fix') >> true
        remote.isIssueFixWord('Fix') >> true
        issue1.getNumber() >> 1
        issue1.getHtmlUrl() >> new URL(issuesUrl + '/' + issue1.getNumber())

        issue2.getNumber() >> 2
        issue2.getHtmlUrl() >> new URL(issuesUrl + '/' + issue2.getNumber())

        issue3.getNumber() >> 3
        issue3.getHtmlUrl() >> new URL(issuesUrl + '/' + issue3.getNumber())

        issue18.getNumber() >> 18
        issue18.getHtmlUrl() >> new URL(otherIssuesUrl + '/' + issue18.getNumber())

        revCommit.getFullMessage() >> fullMessage

    }

    def "standard message"() {
        given:
        String expandedIssue = "Fix [1](https://github.com/davidsowerby/scratch/issues/1)"
        String shortMsg = 'Fix #1 widget properly fixed'
        String fullMsg = shortMsg + '\n\n' + 'messageBody'
        String abbreviatedShortMsg = 'widget properly fixed'
        String expandedFullMessage = expandedIssue + " " + abbreviatedShortMsg + '\n\n' + 'messageBody'
        GitCommit commit = new GitCommit(fullMsg)

        when:
        commit.extractIssueReferences(remote)

        then:
        commit.getIssueReferences().size() == 1
        commit.getIssueReferences().contains(issue1)
        commit.getShortMessage().equals(abbreviatedShortMsg)
        commit.getFullMessage().equals(expandedFullMessage)
    }

    def "standard message, additional fixes in body"() {
        given:
        String shortMsg = 'Fix #1 widget properly fixed'
        String fullMsg = shortMsg + '\n\n' + 'messageBody also fix #2 and fix #3'
        String expandedShortMsg = 'widget properly fixed'
        String expandedFullMessage = "Fix [1](https://github.com/davidsowerby/scratch/issues/1) widget properly fixed" + '\n\n' + 'messageBody also fix [2](https://github.com/davidsowerby/scratch/issues/2) and fix [3](https://github.com/davidsowerby/scratch/issues/3)'
        GitCommit commit = new GitCommit(fullMsg)

        when:
        commit.extractIssueReferences(remote)

        then:
        commit.getIssueReferences().size() == 3
        commit.getIssueReferences().contains(issue1)
        commit.getIssueReferences().contains(issue2)
        commit.getIssueReferences().contains(issue3)
        commit.getFullMessage().equals(expandedFullMessage)
    }

    def "standard message, typo, space missing, typo corrected"() {
        given:
        String shortMsg = 'Fix#1 widget properly fixed'
        String fullMsg = shortMsg + '\n\n' + 'messageBody'
        String expandedShortMsg = 'widget properly fixed'
        String expandedFullMessage = 'Fix [1](https://github.com/davidsowerby/scratch/issues/1) widget properly fixed' + '\n\n' + 'messageBody'
        GitCommit commit = new GitCommit(fullMsg)

        when:
        commit.extractIssueReferences(remote)

        then:
        commit.getIssueReferences().size() == 1
        commit.getIssueReferences().contains(issue1)
        commit.getFullMessage().equals(expandedFullMessage)
    }

    def "Issue number not found by API"() {
        given:
        String shortMsg = 'Fix #99 widget properly fixed'
        String fullMsg = shortMsg + '\n\n' + 'messageBody'
        String expandedShortMsg = shortMsg
        String expandedFullMessage = fullMsg
        GitCommit commit = new GitCommit(fullMsg)

        when:
        commit.extractIssueReferences(remote)

        then:
        commit.getIssueReferences().size() == 0
        commit.getFullMessage().equals(expandedFullMessage)
    }

    def "Issue number invalid format"() {
        given:
        String shortMsg = 'Fix #9a widget properly fixed'
        String fullMsg = shortMsg + '\n\n' + 'messageBody'
        String expandedShortMsg = shortMsg
        String expandedFullMessage = fullMsg
        GitCommit commit = new GitCommit(fullMsg)

        when:
        commit.extractIssueReferences(remote)

        then:
        commit.getIssueReferences().size() == 0
        commit.getFullMessage().equals(expandedFullMessage)
    }

    def "fix in other repo"() {
        given:
        String shortMsg = 'Fix davidsowerby/other#18 widget properly fixed'
        String fullMsg = shortMsg + '\n\n' + 'messageBody'
        String expandedShortMsg = 'Fix [18](https://api.github.com/davidsowerby/other/issues/18) widget properly fixed'
        String expandedFullMessage = expandedShortMsg + '\n\n' + 'messageBody'
        GitCommit commit = new GitCommit(fullMsg)

        when:
        commit.extractIssueReferences(remote)

        then:
        commit.getIssueReferences().size() == 1
        commit.getIssueReferences().contains(issue18)
        commit.extractShortMessage().equals(expandedShortMsg)
        commit.getFullMessage().equals(expandedFullMessage)
    }

    def "invalid reference, other repo name incorrectly structured"() {
        given:
        remote.getIssue("davidsowerby-other", 18) >> { throw (new IOException()) }
        String shortMsg = 'Fix davidsowerby-other#18 widget properly fixed'
        String fullMsg = shortMsg + '\n\n' + 'messageBody'
        String expandedShortMsg = shortMsg
        String expandedFullMessage = fullMsg
        GitCommit commit = new GitCommit(fullMsg)

        when:
        commit.extractIssueReferences(remote)

        then:
        commit.getIssueReferences().size() == 0
        commit.extractShortMessage().equals(shortMsg)
        commit.getFullMessage().equals(fullMsg)
    }

    def "invalid reference, hash with non-number"() {
        given:
        String shortMsg = 'Fix #aa'
        String fullMsg = shortMsg + '\n\n' + 'messageBody'
        String expandedShortMsg = shortMsg
        String expandedFullMessage = fullMsg
        GitCommit commit = new GitCommit(fullMsg)

        when:
        commit.extractIssueReferences(remote)

        then:
        commit.getIssueReferences().size() == 0
        commit.extractShortMessage().equals(shortMsg)
        commit.getFullMessage().equals(fullMsg)
    }

    def "invalid reference, trailing hash"() {
        given:
        String shortMsg = 'rubbish#'
        String fullMsg = shortMsg + '\n\n' + 'messageBody'
        String expandedShortMsg = shortMsg
        String expandedFullMessage = fullMessage
        GitCommit commit = new GitCommit(fullMsg)

        when:
        commit.extractIssueReferences(remote)

        then:
        commit.getIssueReferences().size() == 0
        commit.extractShortMessage().equals(shortMsg)
        commit.getFullMessage().equals(fullMsg)
    }

    def "split"() {
        given:
        String s = "#34"

        when:
        String[] p = s.split("#")

        then:
        println s.length()
    }

    def "equals and hashcode"() {
        given:
        GitCommit commit1 = new GitCommit('a')
        GitCommit commit2 = new GitCommit('b')
        GitCommit commit3 = new GitCommit('c')
        commit1.setHash('hash1')
        commit2.setHash('hash1')
        commit3.setHash('hash2')

        expect:
        commit1.equals(commit2)
        !commit3.equals(commit2)
        commit1.hashCode() == commit2.hashCode()
        commit3.hashCode() != commit2.hashCode()
    }

    def "set and get"() {
        given:
        GitCommit commit = new GitCommit('a')
        PersonIdent ident = Mock(PersonIdent)
        ident.getWhen() >> new Date()
        ident.getTimeZone() >> TimeZone.getTimeZone('Z')

        when:
        commit.setHash('a')
        commit.setAuthor(ident)
        commit.setCommitter(ident)

        then:
        commit.getHash().equals('a')
        commit.getAuthor() == ident
        commit.getCommitter() == ident
    }


}
