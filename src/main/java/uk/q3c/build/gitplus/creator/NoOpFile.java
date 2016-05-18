package uk.q3c.build.gitplus.creator;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by David Sowerby on 18 May 2016
 */
public class NoOpFile implements FileBuilder {

    @Override
    public Optional<File> write() throws IOException {
        return Optional.empty();
    }
}
