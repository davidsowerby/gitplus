package uk.q3c.build.gitplus.gitplus

import org.apache.commons.io.FileUtils
import spock.lang.Specification
import uk.q3c.build.gitplus.GitPlusFactory
import uk.q3c.build.gitplus.util.FilePropertiesLoader

/**
 * Created by David Sowerby on 07 Sep 2017
 */
class DefaultGitPlusTest2 extends Specification {

    GitPlus gitPlus

    def setup() {
        gitPlus = GitPlusFactory.instance
        gitPlus.remote.configuration.repoName('dummy').repoUser('davidsowerby')
    }

    def "properties sources"() {
        when: "default"
        gitPlus.execute()

        then:
        gitPlus.propertiesResolver.loaders.size() == 1
        gitPlus.propertiesResolver.loaders.get(0) instanceof FilePropertiesLoader
        (gitPlus.propertiesResolver.loaders.get(0) as FilePropertiesLoader).source == new File(FileUtils.userDirectory, "gitplus/gitplus.properties")

        when:
        gitPlus.propertiesFromGradle()
        gitPlus.execute()

        then:
        gitPlus.propertiesResolver.loaders.size() == 1
        gitPlus.propertiesResolver.loaders.get(0) instanceof FilePropertiesLoader
        (gitPlus.propertiesResolver.loaders.get(0) as FilePropertiesLoader).source == new File(FileUtils.userDirectory, "gradle/gradle.properties")

        when:
        gitPlus.propertiesFromGitPlus()
        gitPlus.execute()

        then:
        gitPlus.propertiesResolver.loaders.size() == 1
        gitPlus.propertiesResolver.loaders.get(0) instanceof FilePropertiesLoader
        (gitPlus.propertiesResolver.loaders.get(0) as FilePropertiesLoader).source == new File(FileUtils.userDirectory, "gitplus/gitplus.properties")
    }
}
