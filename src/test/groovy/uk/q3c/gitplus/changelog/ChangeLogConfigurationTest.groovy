package uk.q3c.gitplus.changelog

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlus
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.local.GitLocal

import static uk.q3c.gitplus.changelog.ChangeLogConfiguration.OutputTarget.USE_FILE_SPEC

/**
 * Created by David Sowerby on 13 Mar 2016
 */
class ChangeLogConfigurationTest extends Specification {


    ChangeLogConfiguration config;
    GitPlusConfiguration gitPlusConfiguration
    GitPlus gitPlus;
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder();

    def setup() {
        gitPlusConfiguration = new GitPlusConfiguration().remoteRepoFullName('davidsowerby/scratch')
        gitPlusConfiguration.validate()
        config = new ChangeLogConfiguration()
        gitPlus = new GitPlus(gitPlusConfiguration, new GitLocal(gitPlusConfiguration), new GitLocal(gitPlusConfiguration))
    }

    def "defaults"() {
        expect:
        config.getTemplateName().equals(ChangeLog.DEFAULT_TEMPLATE)
        config.exclusionTagOpen.equals('{{')
        config.exclusionTagClose.equals('}}')
        config.separatePullRequests
        config.useTypoMap
        config.typoMap.equals(ChangeLogConfiguration.defaultTypoMap)
        config.getLabelGroups().equals(ChangeLogConfiguration.defaultLabelGroups)
        config.getExclusionTags().isEmpty()
        config.getOutputFilename() == 'changelog.md'
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
        config.useTypoMap(false)
        config.exclusionTags(excludedMessageTags)
        config.pullRequestTitle(pullRequestTitle)

        then:
        config.getOutputFile().equals(new File('.'))
        config.getTypoMap() == typoMap
        config.getTemplateName().equals(templateName)
        config.getLabelGroups() == labelGroups
        config.getExclusionTagOpen().equals(messageTagOpen)
        config.getExclusionTagClose().equals(messageTagClose)
        !config.isSeparatePullRequests()
        !config.isUseTypoMap()
        config.getExclusionTags() == excludedMessageTags
        config.pullRequestTitle.equals(pullRequestTitle)
        config.getOutputFilename().equals(outputFilename)
    }

    def "validate empty"() {
        when:
        config.validate()

        then:
        noExceptionThrown()
    }

    def "validate, using file spec but no output file throws exception"() {
        when:
        config.outputDirectory(USE_FILE_SPEC)
        config.validate()

        then:
        thrown ChangeLogConfigurationException
    }


}
