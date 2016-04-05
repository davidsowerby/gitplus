package uk.q3c.gitplus.util;

/**
 * Created by David Sowerby on 04 Apr 2016
 */
public class MissingPropertyException extends RuntimeException {
    public MissingPropertyException(String key) {
        super("The " + key + " property is missing or has no value");
    }
}
