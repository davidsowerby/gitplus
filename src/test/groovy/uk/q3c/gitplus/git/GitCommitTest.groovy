package uk.q3c.gitplus.git

import org.kohsuke.github.GHIssue
import spock.lang.Specification
import uk.q3c.gitplus.origin.OriginServiceApi

/**
 * Created by David Sowerby on 09 Mar 2016
 */
class GitCommitTest extends Specification {

    OriginServiceApi originServiceApi = Mock(OriginServiceApi)
    GHIssue issue1 = Mock(GHIssue)
    GHIssue issue2 = Mock(GHIssue)
    GHIssue issue3 = Mock(GHIssue)
    GHIssue issue18 = Mock(GHIssue)

    def setup() {
        String repoName = 'davidsowerby/scratch'
        String otherRepoName = 'davidsowerby/other'
        String issuesUrl = "https://github.com/davidsowerby/scratch/issues"
        String otherIssuesUrl = "https://api.github.com/davidsowerby/other/issues"
        originServiceApi.getRepoName() >> repoName
        originServiceApi.getIssue(repoName, 1) >> issue1
        originServiceApi.getIssue(repoName, 2) >> issue2
        originServiceApi.getIssue(repoName, 3) >> issue3
        originServiceApi.getIssue(repoName, 99) >> { throw (new IOException()) }

        originServiceApi.getIssue(otherRepoName, 18) >> issue18

        originServiceApi.isIssueFixWord('fix') >> true
        originServiceApi.isIssueFixWord('Fix') >> true
        issue1.getNumber() >> 1
        issue1.getHtmlUrl() >> new URL(issuesUrl + '/' + issue1.getNumber())

        issue2.getNumber() >> 2
        issue2.getHtmlUrl() >> new URL(issuesUrl + '/' + issue2.getNumber())

        issue3.getNumber() >> 3
        issue3.getHtmlUrl() >> new URL(issuesUrl + '/' + issue3.getNumber())

        issue18.getNumber() >> 18
        issue18.getHtmlUrl() >> new URL(otherIssuesUrl + '/' + issue18.getNumber())


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
        commit.extractIssueReferences(originServiceApi)

        then:
        commit.getFixReferences().size() == 1
        commit.getFixReferences().contains(issue1)
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
        commit.extractIssueReferences(originServiceApi)

        then:
        commit.getFixReferences().size() == 3
        commit.getFixReferences().contains(issue1)
        commit.getFixReferences().contains(issue2)
        commit.getFixReferences().contains(issue3)
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
        commit.extractIssueReferences(originServiceApi)

        then:
        commit.getFixReferences().size() == 1
        commit.getFixReferences().contains(issue1)
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
        commit.extractIssueReferences(originServiceApi)

        then:
        commit.getFixReferences().size() == 0
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
        commit.extractIssueReferences(originServiceApi)

        then:
        commit.getFixReferences().size() == 0
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
        commit.extractIssueReferences(originServiceApi)

        then:
        commit.getFixReferences().size() == 1
        commit.getFixReferences().contains(issue18)
        commit.extractShortMessage().equals(expandedShortMsg)
        commit.getFullMessage().equals(expandedFullMessage)
    }

    def "invalid reference, other repo name incorrectly structured"() {
        given:
        String shortMsg = 'Fix davidsowerby-other#18 widget properly fixed'
        String fullMsg = shortMsg + '\n\n' + 'messageBody'
        String expandedShortMsg = 'Fix [18](https://api.github.com/davidsowerby/other/issues/18) widget properly fixed'
        String expandedFullMessage = expandedShortMsg + '\n\n' + 'messageBody'
        GitCommit commit = new GitCommit(fullMsg)

        when:
        commit.extractIssueReferences(originServiceApi)

        then:
        commit.getFixReferences().size() == 0
        commit.extractShortMessage().equals(shortMsg)
        commit.getFullMessage().equals(fullMsg)
    }
}
