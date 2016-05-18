package uk.q3c.build.gitplus.creator;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import uk.q3c.build.gitplus.gitplus.ProjectCreator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by David Sowerby on 23 Apr 2016
 */
public abstract class ProjectCreatorBase implements ProjectCreator {
    private static Logger log = getLogger(ProjectCreatorBase.class);
    private Map<String, FileBuilder> directories = new HashMap<>();
    private Set<FileBuilder> files = new HashSet<>();
    private File projectDir;

    public ProjectCreatorBase(@Nonnull File projectDir) {
        checkNotNull(projectDir);
        this.projectDir = projectDir;
    }

    /**
     * Specifies a directory to be created and a file to be placed in it (this may just be a dummy file to stop Git from removing an empty directory)
     *
     * @param path        file path to the directory
     * @param fileBuilder the builder to use for the dummy file.  Can be null, in which case a {@link NoOpFile} is used internally
     * @return this for fluency
     */
    public ProjectCreatorBase directory(@Nonnull String path, @Nullable FileBuilder fileBuilder) {
        checkNotNull(path);
        if (fileBuilder == null) {
            directories.put(path, new NoOpFile());
        } else {
            directories.put(path, fileBuilder);
        }
        return this;
    }

    public ProjectCreatorBase file(@Nonnull FileBuilder fileBuilder) {
        checkNotNull(fileBuilder);
        files.add(fileBuilder);
        return this;
    }

    protected File createDirectory(File projectDir, String path, FileBuilder fileBuilder) throws IOException {
        File directory = new File(projectDir, path);
        if (directory.exists()) {
            log.warn("Directory {} already exists, call to create it has been ignored", directory);
        } else {
            FileUtils.forceMkdir(directory);
        }
        if (fileBuilder instanceof DummyFileBuilder) {
            ((DummyFileBuilder) fileBuilder).setDirectory(directory);
        }
        fileBuilder.write();
        return directory;
    }

    @Override
    public void execute() throws IOException {
        checkNotNull(projectDir);
        for (Map.Entry<String, FileBuilder> entry : directories.entrySet()) {
            createDirectory(projectDir, entry.getKey(), entry.getValue());
        }
        for (FileBuilder file : files) {
            file.write();
        }
    }


}
