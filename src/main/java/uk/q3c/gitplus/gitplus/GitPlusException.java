package uk.q3c.gitplus.gitplus;

public class GitPlusException extends RuntimeException {


    public GitPlusException(String message, Exception e) {
        super(message, e);
    }
}
