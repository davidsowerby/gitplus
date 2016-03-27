package uk.q3c.gitplus.changelog

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlus
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.local.GitLocal

/**
 * Created by David Sowerby on 13 Mar 2016
 */
class ChangeLogConfigurationTest extends Specification {

    final String remoteScratchUrl = "https://github.com/davidsowerby/scratch"

    ChangeLogConfiguration config;
    GitPlusConfiguration gitPlusConfiguration
    GitPlus gitHandler;
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder();

    def setup() {
        gitPlusConfiguration = new GitPlusConfiguration().remoteRepoFullName('davidsowerby/scratch')
        config = new ChangeLogConfiguration()
        gitHandler = new GitPlus(gitPlusConfiguration, new GitLocal(gitPlusConfiguration))
    }

    def "defaults"() {
        expect:
        config.getTemplateName().equals(ChangeLog.DEFAULT_TEMPLATE)
        config.messageTagOpen.equals('{{')
        config.messageTagClose.equals('}}')
        config.separatePullRequests
        config.useTypoMap
        config.typoMap.equals(ChangeLogConfiguration.defaultTypoMap)
        config.getLabelGroups().equals(ChangeLogConfiguration.defaultLabelGroups)
        config.getExcludedMessageTags().isEmpty()
        config.getOutputFile() == null
    }

    def "set get"() {
        given:
        Map<String, String> typoMap = new HashMap<>()
        Map<String, Set<String>> labelGroups = new HashMap<>()
        final String templateName = 'wiggly.vm'
        final String messageTagOpen = '[['
        final String messageTagClose = ']]'
        Set<String> excludedMessageTags = new HashSet<>()

        when:
        config.outputFile(new File('.'))
        config.typoMap(typoMap)
        config.templateName(templateName)
        config.labelGroups(labelGroups)
        config.messageTagOpen(messageTagOpen)
        config.messageTagClose(messageTagClose)
        config.separatePullRequests(false)
        config.useTypoMap(false)
        config.excludedMessageTags(excludedMessageTags)

        then:
        config.getOutputFile().equals(new File('.'))
        config.getTypoMap() == typoMap
        config.getTemplateName().equals(templateName)
        config.getLabelGroups() == labelGroups
        config.getMessageTagOpen().equals(messageTagOpen)
        config.getMessageTagClose().equals(messageTagClose)
        !config.isSeparatePullRequests()
        !config.isUseTypoMap()
        config.getExcludedMessageTags() == excludedMessageTags
    }

    def "validate empty"() {
        when:
        config.validate()

        then:
        thrown ChangeLogConfigurationException

        when:
        config.outputFile(new File('.'))
        config.validate()

        then:
        noExceptionThrown()
    }


}
