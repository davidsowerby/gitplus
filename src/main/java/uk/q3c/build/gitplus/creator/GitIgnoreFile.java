package uk.q3c.build.gitplus.creator;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by David Sowerby on 23 Apr 2016
 */
public class GitIgnoreFile extends AbstractFileBuilder<GitIgnoreFile> implements FileBuilder {

    private Set<String> entries = new TreeSet<>();

    public GitIgnoreFile(@Nonnull File projectDir) {
        super(projectDir);
        filename(".gitignore");
    }

    public GitIgnoreFile entry(@Nonnull String entry) {
        checkNotNull(entry);
        entries.add(entry);
        return this;
    }

    @Override
    protected List<String> assemble() {
        return new ArrayList<>(entries);
    }

    public GitIgnoreFile idea() {
        entries.add("*.iml");
        entries.add("/.idea");
        return this;
    }

    public GitIgnoreFile java() {
        entries.add("/build");
        return this;
    }

    public GitIgnoreFile eclipse() {
        entries.add("/.project");
        entries.add("/.classpath");
        return this;
    }

}
