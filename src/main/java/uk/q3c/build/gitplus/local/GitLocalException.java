package uk.q3c.build.gitplus.local;

public class GitLocalException extends RuntimeException {


    public GitLocalException(String message) {
        super(message);
    }

    public GitLocalException(String message, Exception e) {
        super(message, e);
    }
}
