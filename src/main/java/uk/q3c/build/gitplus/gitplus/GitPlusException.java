package uk.q3c.build.gitplus.gitplus;

public class GitPlusException extends RuntimeException {

    public GitPlusException(String message, Exception e) {
        super(message, e);
    }
}
