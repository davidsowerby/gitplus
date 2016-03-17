package uk.q3c.gitplus.changelog

/**
 * Created by David Sowerby on 13 Mar 2016
 */
public class ChangeLogException extends RuntimeException {

    public ChangeLogException(String s) {
        super(s)
    }

    public ChangeLogException(String s, Exception e) {
        super(s, e)
    }
}
