package uk.q3c.build.gitplus.local

import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RemoteRefUpdate
import spock.lang.Specification

/**
 * Created by David Sowerby on 17 Mar 2016
 */
class PushResponseTest extends Specification {

    PushResult pushResult = Mock(PushResult)
    PushResponse pushResponse
    RemoteRefUpdate rru1 = Mock(RemoteRefUpdate)
    RemoteRefUpdate rru2 = Mock(RemoteRefUpdate)
    RemoteRefUpdate rru3 = Mock(RemoteRefUpdate)

    def setup() {
        pushResponse = new PushResponse()
    }

    def "results all successful"() {
        given:
        List<RemoteRefUpdate> remoteUpdates = new ArrayList<>()
        remoteUpdates.add(rru1)
        remoteUpdates.add(rru2)
        remoteUpdates.add(rru3)

        rru1.getStatus() >> RemoteRefUpdate.Status.OK
        rru2.getStatus() >> RemoteRefUpdate.Status.UP_TO_DATE
        rru3.getStatus() >> RemoteRefUpdate.Status.OK
        pushResult.getRemoteUpdates() >> remoteUpdates

        when:
        pushResponse.add(pushResult)

        then:
        pushResponse.isSuccessful()
    }


    def "results - one fails"() {
        given:
        List<RemoteRefUpdate> remoteUpdates = new ArrayList<>()
        remoteUpdates.add(rru1)
        remoteUpdates.add(rru2)
        remoteUpdates.add(rru3)

        rru1.getStatus() >> RemoteRefUpdate.Status.OK
        rru2.getStatus() >> RemoteRefUpdate.Status.UP_TO_DATE
        rru3.getStatus() >> RemoteRefUpdate.Status.NON_EXISTING
        pushResult.getRemoteUpdates() >> remoteUpdates

        when:
        pushResponse.add(pushResult)

        then:
        !pushResponse.isSuccessful()
    }

    def "messages"() {
        given:
        List<RemoteRefUpdate> remoteUpdates = new ArrayList<>()
        remoteUpdates.add(rru1)
        remoteUpdates.add(rru2)
        remoteUpdates.add(rru3)

        rru1.getStatus() >> RemoteRefUpdate.Status.OK
        rru2.getStatus() >> RemoteRefUpdate.Status.UP_TO_DATE
        rru3.getStatus() >> RemoteRefUpdate.Status.NON_EXISTING

        rru1.getRemoteName() >> 'rru1'
        rru2.getRemoteName() >> 'rru2'
        rru3.getRemoteName() >> 'rru3'

        String output =
                'rru1  :  OK' + '\n' +
                        'rru2  :  UP_TO_DATE' + '\n' +
                        'rru3  :  NON_EXISTING' + '\n'

        pushResult.getRemoteUpdates() >> remoteUpdates

        when:
        pushResponse.add(pushResult)

        then:
        pushResponse.messages().equals(output)
    }
}
