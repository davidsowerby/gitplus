package uk.q3c.build.gitplus.creator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import uk.q3c.build.gitplus.creator.gradle.DefaultScriptBlock;
import uk.q3c.build.gitplus.creator.gradle.ElementFactory;
import uk.q3c.build.gitplus.creator.gradle.GradleFile;
import uk.q3c.build.gitplus.creator.gradle.GradleFileContent;

import javax.annotation.Nonnull;
import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates a project directory structure suitable for Java projects which use Spock for testing
 * <p>
 * Created by David Sowerby on 23 Apr 2016
 */
public class JavaSpockProjectCreator extends ProjectCreatorBase {

    private final GradleFileContent gradleFileContent;
    private final GradleFile gradleFile;
    private final GitIgnoreFile gitIgnoreFile;

    public JavaSpockProjectCreator(@Nonnull File projectDir) {
        super(projectDir);
        checkNotNull(projectDir);
        gradleFile = ElementFactory.INSTANCE.gradleFile(projectDir);
        gradleFileContent = gradleFile.getContent();
        gitIgnoreFile = new GitIgnoreFile(projectDir);
    }

    public GradleFileContent getGradleFileContent() {
        return gradleFileContent;
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

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    protected void prepareGradleFile() {
        gradleFileContent.plugins().java().idea().eclipse().maven().mavenPublish();
        gradleFileContent.sourceCompatibility("1.8");
        gradleFileContent.repositories().jcenter();
        gradleFileContent.junit(DefaultScriptBlock.Companion.getTestCompile(), "4.12")
                .spock(DefaultScriptBlock.Companion.getTestCompile(), "1.0-groovy-2.4")
                .groovy(DefaultScriptBlock.Companion.getTestCompile(), "2.4.7")
                .publishing(true);
        file(gradleFile);
    }


}
