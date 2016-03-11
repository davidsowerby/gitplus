package uk.q3c.gitplus.origin;

import java.io.IOException;

/**
 * Created by David Sowerby on 09 Mar 2016
 */
public class OriginServiceException extends RuntimeException {
    public OriginServiceException(String s, IOException e) {
        super(s, e);
    }

    public OriginServiceException(String s) {
        super(s);
    }
}
