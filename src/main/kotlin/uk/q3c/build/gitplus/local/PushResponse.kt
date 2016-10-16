package uk.q3c.build.gitplus.local

import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RemoteRefUpdate
import java.util.*

/**
 * Created by David Sowerby on 19/05/15.
 */
class PushResponse {

    internal var pass: EnumSet<RemoteRefUpdate.Status> = EnumSet.of(RemoteRefUpdate.Status.OK, RemoteRefUpdate.Status.UP_TO_DATE)
    internal val fail: EnumSet<RemoteRefUpdate.Status> = EnumSet.complementOf(pass)
    private val updates = ArrayList<RemoteRefUpdate>()
    private var localFailure: Boolean = false

    fun getUpdates(): List<RemoteRefUpdate> {
        return updates
    }

    fun localFailure(): PushResponse {
        this.localFailure = true
        return this
    }

    fun add(pushResult: PushResult) {
        updates.addAll(pushResult.remoteUpdates)
    }

    val isSuccessful: Boolean
        get() {
            if (localFailure) return false
            for (update in updates) {
                if (fail.contains(update.status)) {
                    return false
                }
            }
            return true
        }

    fun messages(): String {
        val buf = StringBuilder()
        for (update in updates) {
            buf.append(update.remoteName)
            buf.append("  :  ")
            buf.append(update.status)
            buf.append("\n")
        }
        return buf.toString()
    }
}
