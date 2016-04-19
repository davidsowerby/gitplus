package uk.q3c.build.gitplus.local

import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlusConfiguration

/**
 * Created by David Sowerby on 01 Apr 2016
 */
class DefaultGitLocalProviderTest extends Specification {


    def "get"() {
        given:
        GitPlusConfiguration configuration = new GitPlusConfiguration().projectName('dummy')
        GitLocalProvider provider = new DefaultGitLocalProvider()

        expect:
        provider.get(configuration) != null

    }

}
