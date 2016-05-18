package uk.q3c.build.gitplus.creator;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by David Sowerby on 23 Apr 2016
 */
@FunctionalInterface
public interface FileBuilder {

    Optional<File> write() throws IOException;
}
