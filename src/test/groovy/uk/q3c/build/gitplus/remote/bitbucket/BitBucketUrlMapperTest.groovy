package uk.q3c.build.gitplus.remote.bitbucket

import spock.lang.Specification
import uk.q3c.build.gitplus.remote.GitRemote

/**
 * Created by David Sowerby on 31 Oct 2016
 */
class BitBucketUrlMapperTest extends Specification {

    GitRemote remote = Mock(GitRemote)
    BitBucketUrlMapper mapper

    def setup() {
        remote.getRepoUser() >> 'davidsowerby'
        mapper = new BitBucketUrlMapper()
        mapper.owner = remote
    }

    def "CloneUrl"() {
        given:
        remote.providerBaseUrl >> 'bitbucket.com'

        expect:
        mapper.cloneUrl() == "https://davidsowerby@bitbucket.com"
    }
}
