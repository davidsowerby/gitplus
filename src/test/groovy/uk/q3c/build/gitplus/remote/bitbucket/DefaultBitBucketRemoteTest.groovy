package uk.q3c.build.gitplus.remote.bitbucket

import com.google.common.collect.ImmutableMap
import kotlin.NotImplementedError
import spock.lang.Specification
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.remote.DefaultRemoteRequest
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration
import uk.q3c.build.gitplus.remote.RemoteRequest

/**
 * Created by David Sowerby on 31 Oct 2016
 */
class DefaultBitBucketRemoteTest extends Specification {

    BitBucketRemote remote
    GitRemoteConfiguration configuration = Mock(GitRemoteConfiguration)
    BitBucketProvider bitBucketProvider = Mock(BitBucketProvider)
    RemoteRequest remoteRequest = new DefaultRemoteRequest()

    void setup() {
        remote = new DefaultBitBucketRemote(configuration, bitBucketProvider, remoteRequest, new BitBucketUrlMapper())
    }

    def "latestCommit"() {

        when:
        remote.latestCommitSHA(new GitBranch('develop'))

        then:
        thrown NotImplementedError
    }

    def "isIssueFixWord"() {

        when:
        remote.isIssueFixWord('develop')

        then:
        thrown NotImplementedError
    }

    def "getIssue from current remote repo"() {

        when:
        remote.getIssue(5)

        then:
        thrown NotImplementedError
    }

    def "get issue from specified remote repo"() {

        when:
        remote.getIssue('user', 'repo', 5)

        then:
        thrown NotImplementedError
    }

    def "getcredentialsProvider"() {

        when:
        remote.getCredentialsProvider()

        then:
        thrown NotImplementedError
    }

    def "apiStatus"() {

        when:
        remote.apiStatus()

        then:
        thrown NotImplementedError
    }

    def "createIssue"() {

        when:
        remote.createIssue('title', 'develop', 'bug')

        then:
        thrown NotImplementedError
    }

    def "createRepo"() {

        when:
        remote.createRepo()

        then:
        thrown NotImplementedError
    }

    def "delete repo"() {

        when:
        remote.deleteRepo()

        then:
        thrown NotImplementedError
    }

    def "listRepositoryNames"() {

        when:
        remote.listRepositoryNames()

        then:
        thrown NotImplementedError
    }

    def "get mergeLabels"() {

        when:
        remote.mergeLabels()

        then:
        thrown NotImplementedError
    }

    def "getLabelsAsMap"() {

        when:
        remote.getLabelsAsMap()

        then:
        thrown NotImplementedError
    }

    def "latestDevelopCommit"() {

        when:
        remote.latestDevelopCommitSHA()

        then:
        thrown NotImplementedError
    }

    def "mergeLabels"() {
        given:
        Map<String, String> map = ImmutableMap.of()

        when:
        remote.mergeLabels(map)

        then:
        thrown NotImplementedError
    }

    def "hasBranch"() {
        when:
        remote.hasBranch(new GitBranch("master"))

        then:
        thrown NotImplementedError
    }
}
