package uk.q3c.build.gitplus.changelog

import com.google.common.collect.ImmutableSet
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.local.GitLocalProvider
/**
 * Created by David Sowerby on 13 Mar 2016
 */
class ChangeLogConfigurationTest extends Specification {


    ChangeLogConfiguration config;
    GitPlus gitPlus;
    GitLocalProvider gitLocalProvider = Mock(GitLocalProvider)
    GitLocal gitLocal = Mock(GitLocal)
    GitLocal wikiLocal = Mock(GitLocal)
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder();

    def setup() {
        gitLocalProvider.get(_) >>> [gitLocal, wikiLocal]
        config = new ChangeLogConfiguration()
        gitPlus = new GitPlus()
        gitPlus.getConfiguration().remoteRepoFullName('davidsowerby/scratch')
    }

    def "defaults"() {
        expect:
        config.getTemplateName().equals(ChangeLogConfiguration.DEFAULT_TEMPLATE)
        config.exclusionTagOpen.equals('{{')
        config.exclusionTagClose.equals('}}')
        config.separatePullRequests
        config.correctTypos
        config.typoMap.equals(ChangeLogConfiguration.defaultTypoMap)
        config.getLabelGroups().equals(ChangeLogConfiguration.defaultLabelGroups)
        config.getExclusionTags().equals(ImmutableSet.of('javadoc'))
        config.getOutputFilename() == 'changelog.md'
        config.getFromVersion().equals(ChangeLogConfiguration.LATEST_COMMIT)
    }

    def "set get"() {
        given:
        Map<String, String> typoMap = new HashMap<>()
        Map<String, Set<String>> labelGroups = new HashMap<>()
        final String templateName = 'wiggly.vm'
        final String messageTagOpen = '[['
        final String messageTagClose = ']]'
        Set<String> excludedMessageTags = new HashSet<>()
        String pullRequestTitle = 'Pulleys'
        String outputFilename = 'filename.md'

        when:
        config.outputFile(new File('.'))
        config.outputFileName(outputFilename)
        config.typoMap(typoMap)
        config.templateName(templateName)
        config.labelGroups(labelGroups)
        config.exclusionTagOpen(messageTagOpen)
        config.exclusionTagClose(messageTagClose)
        config.separatePullRequests(false)
        config.correctTypos(false)
        config.exclusionTags(excludedMessageTags)
        config.pullRequestTitle(pullRequestTitle)
        config.fromVersion('0.1')
        config.toVersion('0.2')
        config.numberOfVersions(33)

        then:
        config.getOutputFile().equals(new File('.'))
        config.getTypoMap() == typoMap
        config.getTemplateName().equals(templateName)
        config.getLabelGroups() == labelGroups
        config.getExclusionTagOpen().equals(messageTagOpen)
        config.getExclusionTagClose().equals(messageTagClose)
        !config.isSeparatePullRequests()
        !config.isCorrectTypos()
        config.getExclusionTags() == excludedMessageTags
        config.pullRequestTitle.equals(pullRequestTitle)
        config.getOutputFilename().equals(outputFilename)
        config.getFromVersion().equals('0.1')
        config.isFromVersion('0.1')
        !config.isFromVersion('0.2')
        config.getToVersion().equals('0.2')
        config.isToVersion('0.2')
        !config.isToVersion('0.1')
        config.getNumberOfVersions() == 33
    }


    def "toVersion is null, returns false"() {
        expect:
        !config.isToVersion('1')
    }

    def "toVersion with null parameter throws NPE"() {
        given:
        config.toVersion('1')
        when:
        config.isToVersion(null)

        then:
        thrown NullPointerException
    }

    def "fromVersion is null, returns false"() {
        expect:
        !config.isFromVersion('1')
    }

    def "fromVersion with null parameter throws NPE"() {
        given:
        config.fromVersion('1')
        when:
        config.isFromVersion(null)

        then:
        thrown NullPointerException
    }

    def "fromLatestVersion"() {
        expect:
        !config.fromVersion(ChangeLogConfiguration.LATEST_COMMIT).fromLatestVersion()
        config.fromVersion(ChangeLogConfiguration.LATEST_VERSION).fromLatestVersion()
        !config.fromVersion('1').fromLatestVersion()
    }

    def "fromLatestCommit"() {
        expect:
        config.fromVersion(ChangeLogConfiguration.LATEST_COMMIT).fromLatestCommit()
        !config.fromVersion(ChangeLogConfiguration.LATEST_VERSION).fromLatestCommit()
        !config.fromVersion('1').fromLatestCommit()
    }

    def "getOutputFile exception when USE_FILE_SPEC but no output file specified"() {
        given:
        config.outputFile(null).outputTarget(ChangeLogConfiguration.OutputTarget.USE_FILE_SPEC)

        when:
        config.getOutputFile()

        then:
        thrown ChangeLogConfigurationException
    }

    def "getOutputFile exception when not USE_FILE_SPEC and no file name specified"() {
        given:
        config.outputFileName(null).outputTarget(ChangeLogConfiguration.OutputTarget.CURRENT_DIR)

        when:
        config.getOutputFile()

        then:
        thrown ChangeLogConfigurationException
    }

    def "getOutputFile when not USE_FILE_SPEC, has file name, just returns null"() {
        given:
        config.outputFile(null).outputTarget(ChangeLogConfiguration.OutputTarget.PROJECT_BUILD_ROOT).outputFileName('anything')

        when:
        File result = config.getOutputFile()

        then:
        result == null

    }

}
