package uk.q3c.build.gitplus.creator;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import uk.q3c.build.gitplus.gitplus.ProjectCreator;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by David Sowerby on 23 Apr 2016
 */
public abstract class ProjectCreatorBase implements ProjectCreator {
    private static Logger log = getLogger(ProjectCreatorBase.class);
    private Set<String> directories = new HashSet<>();
    private Set<FileBuilder> files = new HashSet<>();
    private File projectDir;

    public ProjectCreatorBase(@Nonnull File projectDir) {
        checkNotNull(projectDir);
        this.projectDir = projectDir;
    }

    public ProjectCreatorBase directory(@Nonnull String path) {
        checkNotNull(path);
        directories.add(path);
        return this;
    }

    public ProjectCreatorBase file(@Nonnull FileBuilder fileBuilder) {
        checkNotNull(fileBuilder);
        files.add(fileBuilder);
        return this;
    }

    protected File createDirectory(File projectDir, String path) throws IOException {
        File f = new File(projectDir, path);
        if (f.exists()) {
            log.warn("Directory {} already exists, call to create it has been ignored", f);
        } else {
            FileUtils.forceMkdir(f);
        }
        return f;
    }

    @Override
    public void execute() throws IOException {
        checkNotNull(projectDir);
        for (String directory : directories) {
            createDirectory(projectDir, directory);
        }
        for (FileBuilder file : files) {
            file.write();
        }
    }


}
