package uk.q3c.build.gitplus.creator;

import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by David Sowerby on 23 Apr 2016
 */
public abstract class AbstractFileBuilder<FILE_TYPE> implements FileBuilder {

    protected File projectDir;
    protected String filename;

    public AbstractFileBuilder(@Nonnull File projectDir) {
        checkNotNull(projectDir);
        this.projectDir = projectDir;
    }

    @Override
    public Optional<File> write() throws IOException {
        File f = new File(projectDir, filename);
        FileUtils.writeLines(f, assemble());
        return Optional.of(f);
    }

    /**
     * Assemble all the inputs for file generation
     *
     * @return the contents of the file as a list of lines
     */
    protected abstract List<String> assemble();

    @SuppressWarnings("unchecked")
    public FILE_TYPE filename(final String filename) {
        this.filename = filename;
        return (FILE_TYPE) this;
    }

    public String getFilename() {
        return filename;
    }
}
