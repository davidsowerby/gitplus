package uk.q3c.gitplus.git;

import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by David Sowerby on 19/05/15.
 */
public class PushResponse {

    EnumSet<RemoteRefUpdate.Status> pass = EnumSet.of(RemoteRefUpdate.Status.OK, RemoteRefUpdate.Status.UP_TO_DATE);
    EnumSet<RemoteRefUpdate.Status> fail = EnumSet.complementOf(pass);

    private List<RemoteRefUpdate> updates = new ArrayList<>();

    public void add(PushResult pushResult) {
        updates.addAll(pushResult.getRemoteUpdates());
    }

    public boolean isSuccessful() {
        for (RemoteRefUpdate update : updates) {
            if (fail.contains(update.getStatus())) {
                return false;
            }
        }
        return true;
    }

    public String messages() {
        StringBuilder buf = new StringBuilder();
        for (RemoteRefUpdate update : updates) {
            //            if (fail.contains(update.getStatus())) {
            buf.append(update.getRemoteName() + "  :  " + update.getStatus() + "\n");
            //            }
        }
        return buf.toString();
    }
}
