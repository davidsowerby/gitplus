package uk.q3c.build.gitplus.creator;

import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by David Sowerby on 18 May 2016
 */
public class DummyFileBuilder implements FileBuilder {

    private final String filename;
    private List<String> lines;
    private File directory;

    public DummyFileBuilder(@Nonnull String filename) {
        this.filename = filename;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    protected List<String> assemble() {
        return lines = new ArrayList<>();
    }

    @Override
    public Optional<File> write() throws IOException {
        File f = new File(directory, filename);
        FileUtils.writeLines(f, lines);
        return Optional.of(f);
    }
}
