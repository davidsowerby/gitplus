package uk.q3c.gitplus.gitplus;

/**
 * Created by David Sowerby on 14 Mar 2016
 */
public class GitPlusConfigurationException extends RuntimeException {
    public GitPlusConfigurationException(String msg) {
        super(msg);
    }

    public GitPlusConfigurationException(String s, Exception e) {
        super(s, e);
    }
}
