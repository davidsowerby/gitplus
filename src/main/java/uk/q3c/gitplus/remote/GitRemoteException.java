package uk.q3c.gitplus.remote;

/**
 * Created by David Sowerby on 09 Mar 2016
 */
public class GitRemoteException extends RuntimeException {
    public GitRemoteException(String s, Exception e) {
        super(s, e);
    }

    public GitRemoteException(String s) {
        super(s);
    }
}
