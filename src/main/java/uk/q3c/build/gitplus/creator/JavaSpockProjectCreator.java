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
    private final GitIgnoreFile gitIgnoreFle;

    public JavaSpockProjectCreator(@Nonnull File projectDir) {
        super(projectDir);
        checkNotNull(projectDir);
        gradleFile = new GradleFile(projectDir);
        gitIgnoreFle = new GitIgnoreFile(projectDir);
    }

    public GradleFile getGradleFile() {
        return gradleFile;
    }

    public GitIgnoreFile getGitIgnoreFle() {
        return gitIgnoreFle;
    }

    @Override
    public void prepare() {
        directory("src/main/java");
        directory("src/main/resources");
        directory("src/test/groovy");
        directory("src/test/resources");
        prepareGradleFile();
        prepareGitIgnoreFile();
    }

    protected void prepareGitIgnoreFile() {
        gitIgnoreFle.java()
                    .eclipse()
                    .idea();
        file(gitIgnoreFle);
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
                  .publishing(true);
        file(gradleFile);
    }


}
