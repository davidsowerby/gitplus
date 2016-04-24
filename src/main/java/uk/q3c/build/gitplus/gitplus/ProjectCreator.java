package uk.q3c.build.gitplus.gitplus;

import java.io.IOException;

/**
 * Created by David Sowerby on 15 Mar 2016
 */
public interface ProjectCreator {
    /**
     * Prepares the output without executing - this allows sub-classes to add to, or modify the output before calling execute
     */
    void prepare();

    /**
     * Executes the output prepared by {@link #prepare)}.  If you forget to call {@link #prepare)} first, this call will fail
     *
     */
    void execute() throws IOException;
}
