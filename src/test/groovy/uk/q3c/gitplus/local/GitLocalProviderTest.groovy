package uk.q3c.gitplus.local

import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlusConfiguration

/**
 * Created by David Sowerby on 01 Apr 2016
 */
class GitLocalProviderTest extends Specification {


    def "get"() {
        given:
        GitPlusConfiguration configuration = new GitPlusConfiguration().projectName('dummy')
        GitLocalProvider provider = new GitLocalProvider()

        expect:
        provider.get(configuration) != null

    }

}
