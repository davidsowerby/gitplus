package uk.q3c.build.gitplus.local

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import org.eclipse.jgit.lib.PersonIdent
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.guice.UseModules
import spock.lang.Specification
import uk.q3c.build.gitplus.GitPlusModule
import uk.q3c.build.gitplus.gitplus.GitPlus

import java.time.ZonedDateTime
/**
 * It is impossible to mock a RevCommit (it is a class with embedded static calls, so this unit test is actually more like an integration test
 *
 * Created by David Sowerby on 31 Oct 2016
 */
@UseModules([GitPlusModule])
class GitCommitTest extends Specification {


    @Rule
    TemporaryFolder temporaryFolder
    File temp

    @Inject
    GitPlus gitPlus

    def setup() {
        temp = temporaryFolder.getRoot()
    }


    def "construct, equals and hashcode"() {
        given:
        gitPlus.local.projectDirParent(temp).cloneFromRemote(true).projectName('q3c-testUtil')
        gitPlus.remote.repoUser('davidsowerby').repoName('q3c-testUtil')
        gitPlus.execute()

        when:
        ImmutableList<GitCommit> commits = gitPlus.local.extractDevelopCommits() // develop is default branch
        GitCommit commit1 = commits.get(commits.size() - 1)
        GitCommit commit2 = commits.get(commits.size() - 2)
        GitCommit commit3 = commits.get(commits.size() - 1)



        then:
        commit1.hash == "dc1840e49fd9b19e12a6fcf1684299c36af7624f"
        commit1.fullMessage == "extracted common TestBench code to separate testbench sub-project\n" +
                "closes #181\n" +
                "created a separate testUtils sub-project but not used for anything yet\n" +
                "updated README.md"
        commit1.getAuthor().getName() == 'David Sowerby'
        commit1.getAuthorDate() == ZonedDateTime.parse('2014-03-26T21:42:50Z[GMT]')
        commit1.getCommitDate() == ZonedDateTime.parse('2014-03-26T21:42:50Z[GMT]')
        commit1.getCommitter().getName() == 'David Sowerby'
        commit1.getShortMessage() == "extracted common TestBench code to separate testbench sub-project"
        commit1.equals(commit3)
        !commit1.equals(commit2)
        !commit1.equals(null)
    }

    def "short message"() {
        given:
        PersonIdent personIdent = new PersonIdent("me", "me@there", new Date().toInstant().toEpochMilli(), 0)
        GitCommit commit1 = new GitCommit("line1\nline2", "hash", personIdent, personIdent)

        expect:
        commit1.shortMessage == "line1"
    }
}
