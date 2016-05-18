package uk.q3c.build.gitplus.creator;

import javax.annotation.Nonnull;
import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates a project directory structure suitable for Java projects which use Spock for testing
 * <p>
 * Created by David Sowerby on 23 Apr 2016
 */
public class JavaSpockProjectCreator extends ProjectCreatorBase {

    private final GradleFile gradleFile;
    private final GitIgnoreFile gitIgnoreFile;

    public JavaSpockProjectCreator(@Nonnull File projectDir) {
        super(projectDir);
        checkNotNull(projectDir);
        gradleFile = new GradleFile(projectDir);
        gitIgnoreFile = new GitIgnoreFile(projectDir);
    }

    public GradleFile getGradleFile() {
        return gradleFile;
    }

    public GitIgnoreFile getGitIgnoreFile() {
        return gitIgnoreFile;
    }

    @Override
    public void prepare() {
        directory("src/main/java", new DummyFileBuilder("DummyJava.java"));
        directory("src/main/resources", new DummyFileBuilder("DummyResource.txt"));
        directory("src/test/groovy", new DummyFileBuilder("DummyTestGroovy.groovy"));
        directory("src/test/resources", new DummyFileBuilder("DummyTestResource.txt"));
        prepareGradleFile();
        prepareGitIgnoreFile();
    }

    protected void prepareGitIgnoreFile() {
        gitIgnoreFile.java()
                     .eclipse()
                     .idea();
        file(gitIgnoreFile);
    }

    protected void prepareGradleFile() {
        gradleFile.plugin("java")
                  .plugin("idea")
                  .plugin("eclipse-wtp")
                  .plugin("maven")
                  .plugin("maven-publish")
                  .sourceCompatibility("1.8")
                  .jcenter()
                  .junit()
                  .spock()
                  .groovy()
                  .publishing(true);
        file(gradleFile);
    }


}
