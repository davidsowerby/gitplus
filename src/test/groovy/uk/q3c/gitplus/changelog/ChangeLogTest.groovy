package uk.q3c.gitplus.changelog

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlus
import uk.q3c.gitplus.local.GitCommit
import uk.q3c.gitplus.local.GitLocal
import uk.q3c.gitplus.local.Tag
import uk.q3c.gitplus.remote.GitRemote
import uk.q3c.gitplus.remote.Issue
import uk.q3c.util.testutil.FileTestUtil

import java.nio.file.Paths
import java.time.ZoneId
import java.time.ZonedDateTime

import static uk.q3c.gitplus.changelog.ChangeLogConfiguration.OutputTarget.*

/**
 * Created by David Sowerby on 07 Mar 2016
 */
class ChangeLogTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    ChangeLog changeLog
    ChangeLogConfiguration changeLogConfiguration = Mock(ChangeLogConfiguration)
    GitPlus gitPlus = Mock(GitPlus)
    GitLocal gitLocal = Mock(GitLocal)
    GitLocal wikiLocal = Mock(GitLocal)
    List<Tag> tags
    List<GitCommit> commits
    List<Issue> issues
    List<Set<String>> labels
    GitRemote gitRemote = Mock(GitRemote)
    final String projectFolderName = 'project'
    final String wikiFolderName = 'project.wiki'
    static File projectFolder
    static File wikiFolder


    def setup() {

        tags = new ArrayList<>()
        temp = temporaryFolder.getRoot()
        gitPlus.getGitLocal() >> gitLocal
        gitPlus.getWikiLocal() >> wikiLocal
        projectFolder = new File(temp, projectFolderName)
        wikiFolder = new File(temp, wikiFolderName)
        gitLocal.getProjectDir() >> projectFolder
        wikiLocal.getProjectDir() >> wikiFolder


    }


    def "constructor validates configuration, verifies repo"() {
        given:
        changeLogConfiguration.getOutputFilename() >> temp

        when:
        new ChangeLog(gitPlus, changeLogConfiguration)

        then:
        1 * changeLogConfiguration.validate()
        1 * changeLogConfiguration.getTemplateName() >> ChangeLog.DEFAULT_TEMPLATE
        1 * gitPlus.createOrVerifyRepos()
    }

    def "NPE from constructor is parameter null"() {
        when:
        new ChangeLog(null, changeLogConfiguration)

        then:
        thrown NullPointerException

        when:
        new ChangeLog(gitPlus, null)

        then:
        thrown NullPointerException
    }

    def "not really a test, just checking test setup"() {
        given:
        createDataWithMostRecentCommitTagged()
        gitPlus()

        expect:
        gitPlus.getTags().size() == 5
        commits.size() == 10
        tags.get(0).getCommit() == commits.get(1)
        tags.get(4).getCommit() == commits.get(9)
        gitPlus.extractDevelopCommits().iterator().next() == commits.get(9)
        commits.get(0).getIssueReferences().size() == 2
        commits.get(2).getIssueReferences().size() == 0
        commits.get(0).getIssueReferences().get(0).getLabels() == labels.get(0)
        commits.get(0).getIssueReferences().get(1).getLabels() == labels.get(1)
    }

    def "changelog with mock data"() {
        given:
        changeLogConfiguration.getPullRequestTitle() >> ChangeLogConfiguration.DEFAULT_PULL_REQUESTS_TITLE
        changeLogConfiguration.getOutputDirectory() >> USE_FILE_SPEC
        changeLogConfiguration.getOutputFile() >> new File(temp, 'changelog.md')
        createDataWithMostRecentCommitTagged()
        gitPlus()
        changeLog = new ChangeLog(gitPlus, changeLogConfiguration)
        URL url = this.getClass()
                .getResource('changelog.md');
        File expectedResult = Paths.get(url.toURI())
                .toFile();

        when:
        changeLog.createChangeLog()

        then:
        !FileTestUtil.compare(changeLogConfiguration.getOutputFile(), expectedResult).isPresent()
        changeLog.getVersionRecords().size() == 5
    }

    def "getOutputFile using FILE_SPEC"() {
        given:
        changeLogConfiguration.getOutputDirectory() >> USE_FILE_SPEC
        changeLogConfiguration.getOutputFile() >> new File(temp, 'changelog.md')
        changeLogConfiguration.getTemplateName() >> 'markdown.vm'
        changeLog = new ChangeLog(gitPlus, changeLogConfiguration)

        expect:
        changeLog.getOutputFile().equals(new File(temp, 'changelog.md'))
    }

    def "getOutputFile using PROJECT_ROOT"() {
        given:
        changeLogConfiguration.getOutputDirectory() >> PROJECT_ROOT
        changeLogConfiguration.getOutputFile() >> new File(temp, 'changelog.md')
        changeLogConfiguration.getTemplateName() >> 'markdown.vm'
        changeLogConfiguration.getOutputFilename() >> 'changelog.md'
        changeLog = new ChangeLog(gitPlus, changeLogConfiguration)

        expect:
        changeLog.getOutputFile().equals(new File(projectFolder, 'changelog.md'))
    }

    def "getOutputFile using PROJECT_BUILD_ROOT"() {
        given:
        changeLogConfiguration.getOutputDirectory() >> PROJECT_BUILD_ROOT
        changeLogConfiguration.getOutputFile() >> new File(temp, 'changelog.md')
        changeLogConfiguration.getTemplateName() >> 'markdown.vm'
        changeLogConfiguration.getOutputFilename() >> 'changelog.md'
        changeLog = new ChangeLog(gitPlus, changeLogConfiguration)

        File buildFolder = new File(projectFolder, 'build')

        expect:
        changeLog.getOutputFile().equals(new File(buildFolder, 'changelog.md'))
    }

    def "getOutputFile using WIKI_ROOT"() {
        given:
        changeLogConfiguration.getOutputDirectory() >> WIKI_ROOT
        changeLogConfiguration.getOutputFile() >> new File(temp, 'changelog.md')
        changeLogConfiguration.getTemplateName() >> 'markdown.vm'
        changeLogConfiguration.getOutputFilename() >> 'changelog.md'
        changeLog = new ChangeLog(gitPlus, changeLogConfiguration)

        expect:
        changeLog.getOutputFile().equals(new File(wikiFolder, 'changelog.md'))
    }

    def "getOutputFile using CURRENT_DIR"() {
        given:
        changeLogConfiguration.getOutputDirectory() >> CURRENT_DIR
        changeLogConfiguration.getOutputFile() >> new File(temp, 'changelog.md')
        changeLogConfiguration.getTemplateName() >> 'markdown.vm'
        changeLogConfiguration.getOutputFilename() >> 'changelog.md'
        changeLog = new ChangeLog(gitPlus, changeLogConfiguration)

        File currentDir = new File('.')

        expect:
        changeLog.getOutputFile().equals(new File(currentDir, 'changelog.md'))
    }

    def "currrentBuildTag"() {

    }

/**
 * This creates:<ol>
 * <li>release 0.1 with tag 0 at commit 1, version includes commit 0,1, issues 0,1,2</li>
 * <li>release 0.2 with tag 1 at commit 3, version includes commit 2,3, issues 3,4,5,6,7</li>
 * </ol>
 */
    private void createDataWithMostRecentCommitTagged() {
        createCommits(1, 3, 5, 8, 9)
        issues.get(2).pullRequest(true)
    }

/**
 * Commits 0-9, oldest to newest, hash the same as index.  Needs to be reversed to represent the default order returned by a call to Git log
 */
    private void createCommits(int ... tagged) {
        createIssues()
        List<Integer> numberOfIssuesToAssign = ImmutableList.of(2, 1, 0, 5, 3, 3, 2, 1, 3, 0) //total 20
        commits = new ArrayList<>()
        int tagIndex = 0
        int issueIndex = 0
        for (int i = 0; i < 10; i++) {
            GitCommit commit = newCommit(i)
            commits.add(commit)
            commit.getShortMessage() >> 'commit ' + i + ' short message'
            List<Issue> issueReferences = new ArrayList<>()
            for (int j = 0; j < numberOfIssuesToAssign.get(i); j++) {
                issueReferences.add(issues.get(issueIndex))
                issueIndex++
            }
            commit.getIssueReferences() >> issueReferences

            if (tagged.contains(i)) {
                createTag(tagIndex, commit)
                tagIndex++
            }
        }
    }

    private GitCommit newCommit(int hash) {
        GitCommit commit = Mock(GitCommit)
        commit.getHash() >> Integer.toString(hash)
        return commit
    }

    private void createTag(int index, GitCommit commit) {
        ZonedDateTime zdt = ZonedDateTime.of(2016, 2, 3, 12, 11, 15, 0, ZoneId.of("Z"))
        List<String> tagNames = ImmutableList.of('0.1', '0.2', '1.3.1', '1.3.12', '2.0')
        List<ZonedDateTime> releaseDates = ImmutableList.of(zdt, zdt.plusDays(1), zdt.plusDays(2), zdt.plusDays(3), zdt.plusDays(4))
        Tag tag = Mock(Tag)
        tags.add(tag)
        tag.getCommit() >> commit
        tag.getTagName() >> tagNames.get(index)
        tag.getReleaseDate() >> releaseDates.get(index)
    }

    private GitPlus gitPlus() {
        gitPlus = Mock(GitPlus)
        gitPlus.getTags() >> tags
        gitPlus.extractDevelopCommits() >> ImmutableSet.copyOf(ImmutableList.copyOf(commits).reverse())
        gitPlus.getRemoteTagUrl() >> 'https://github.com/davidsowerby/dummy/tree'
        gitPlus.getGitRemote() >> gitRemote
        gitPlus.getProjectName() >> 'Dummy'
        changeLogConfiguration.getOutputFilename() >> new File(temp, 'changelog.md')
        changeLogConfiguration.getTemplateName() >> ChangeLog.DEFAULT_TEMPLATE
        changeLogConfiguration.getLabelGroups() >> ChangeLogConfiguration.defaultLabelGroups
    }


    private List<Set<String>> createLabels() {
        List<Set<String>> labels
        labels = new ArrayList<>()
        labels.add(ImmutableSet.of('bug'))
        labels.add(ImmutableSet.of('bug', 'build'))
        labels.add(ImmutableSet.of('task'))
        labels.add(ImmutableSet.of('enhancement'))
        labels.add(ImmutableSet.of('testing'))
        labels.add(ImmutableSet.of('rubbish'))
        labels.add(ImmutableSet.of())// deliberately empty
        labels.add(ImmutableSet.of('task', 'build'))
        labels.add(ImmutableSet.of('quality'))
        labels.add(ImmutableSet.of('documentation'))
        labels.add(ImmutableSet.of('bug'))
        labels.add(ImmutableSet.of('bug'))
        labels.add(ImmutableSet.of('bug', 'build'))
        labels.add(ImmutableSet.of('task'))
        labels.add(ImmutableSet.of('bug'))
        labels.add(ImmutableSet.of('performance', 'enhancement')) // 2 in same group
        labels.add(ImmutableSet.of('enhancement', 'documentation')) // 2 in different groups
        labels.add(ImmutableSet.of('bug'))
        labels.add(ImmutableSet.of('enhancement'))
        labels.add(ImmutableSet.of('bug'))
        return labels
    }

    private void createIssues() {
        labels = createLabels()
        issues = new ArrayList<>()
        for (int i = 0; i < 20; i++) {
            Issue issue = new Issue(i)
            issue.title("issue " + i)
                    .labels(labels.get(i))
                    .htmlUrl("https:/github.com/davidsowerby/dummmy/issues/" + i)
            issues.add(issue)
        }
    }


}