package uk.q3c.gitplus.util;

/**
 * Created by David Sowerby on 11 Mar 2016
 */
public class PropertyException extends RuntimeException {

    public PropertyException(String propertyName) {
        super(propertyName + " must be set");
    }
}
