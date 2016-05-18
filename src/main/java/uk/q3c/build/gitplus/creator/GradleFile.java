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
public class GradleFile extends AbstractFileBuilder<GradleFile> implements FileBuilder {

    private final int indentSpaces = 4;
    private List<String> repoNames = new ArrayList<>();
    private Set<String> pluginNames = new TreeSet<>();
    private List<String> testCompileDependencies = new ArrayList<>();
    private List<String> compileDependencies = new ArrayList<>();
    private String sourceCompatibility;
    private boolean publishing;
    private int indent = 0;

    public GradleFile(@Nonnull File projectDir) {
        super(projectDir);
        filename("build.gradle"); // default
    }

    public boolean isPublishing() {
        return publishing;
    }

    public GradleFile publishing(final boolean publishing) {
        this.publishing = publishing;
        return this;
    }

    protected GradleFile repository(@Nonnull String repoName) {
        checkNotNull(repoName);
        repoNames.add(repoName);
        return this;
    }

    public GradleFile plugin(@Nonnull String pluginName) {
        checkNotNull(pluginName);
        pluginNames.add(pluginName);
        return this;
    }

    public GradleFile jcenter() {
        return repository("jcenter()");
    }

    public GradleFile idea() {
        return plugin("idea");
    }

    public GradleFile eclipse() {
        return plugin("eclipse-wtp");
    }


    public GradleFile mavenCentral() {
        return repository("mavenCentral()");
    }

    public GradleFile mavenLocal() {
        return repository("mavenLocal()");
    }

    public GradleFile java() {
        return plugin("java");
    }

    public GradleFile groovy() {
        compileDependency("org.codehaus.groovy:groovy-all:2.4.4");
        return plugin("groovy");
    }

    public GradleFile junit() {
        return testCompileDependency("junit:junit:4.12");
    }

    public GradleFile testCompileDependency(@Nonnull String dependency) {
        checkNotNull(dependency);
        testCompileDependencies.add(dependency);
        return this;
    }

    public GradleFile compileDependency(@Nonnull String dependency) {
        checkNotNull(dependency);
        compileDependencies.add(dependency);
        return this;
    }

    public GradleFile spock() {
        testCompileDependency("org.spockframework:spock-core:1.0-groovy-2.4");
        testCompileDependency("cglib:cglib-nodep:3.2.0");   // for Spock mocks
        testCompileDependency("org.objenesis:objenesis:2.2");  // for Spock mocks
        return this;
    }

    public GradleFile sourceCompatibility(@Nonnull String level) {
        checkNotNull(level);
        sourceCompatibility = level;
        return this;
    }

    @Override
    public List<String> assemble() {

        line("//noinspection GrPackage");
        plugins();
        sourceCompatibility();
        repositories();
        dependencies();
        if (isPublishing()) {
            publishing();
        }
        return contents;
    }


    private void dependencies() {
        startBlock("dependencies");
        dependencyList(compileDependencies, "compile");
        dependencyList(testCompileDependencies, "testCompile");
        endBlock();
    }

    private void dependencyList(List<String> dependencies, String scope) {
        for (String dependency : dependencies) {
            line(scope + " '" + dependency + "'");
        }
    }

    private void plugins() {
        startBlock("plugins");
        for (String plugin : pluginNames) {
            line("id '" + plugin + "'");
        }
        endBlock();
    }

    private void repositories() {
        startBlock("repositories");
        for (String repoName : repoNames) {
            String trimmed = repoName.trim();
            if (trimmed.endsWith("()")) {
                line(repoName);
            } else {
                line("'" + repoName + "'");
            }
        }
        endBlock();
    }

    private void sourceCompatibility() {
        contents.add("");
        if (sourceCompatibility != null) {
            line("sourceCompatibility = '" + sourceCompatibility + "'");
        }
    }

    private void endBlock() {
        indent--;
        line("}");
    }

    private void publishing() {
        plugin("maven");
        plugin("maven-publishing");
        startBlock("publishing");
        startBlock("publications");
        startBlock("mavenStuff(MavenPublication)");
        line("from components.java");

        startBlock("artifact sourcesJar");
        line("classifier 'sources'");
        endBlock();

        startBlock("artifact javadocJar");
        line("classifier 'javadoc'");
        endBlock();

        endBlock(); //mavenStuff
        endBlock(); //publications
        endBlock(); //publishing
        taskSourcesJar();
        taskJavadocJar();
        artifacts();
        javadoc();
    }

    private void javadoc() {
        startBlock("javadoc");
        line("failOnError = false");
        endBlock();
    }

    private void artifacts() {
        startBlock("artifacts");
        line("archives sourcesJar");
        line("archives javadocJar");
        endBlock();
    }

    private void taskJavadocJar() {
        startBlock("task javadocJar(type: Jar, dependsOn: javadoc)");
        line("classifier = 'javadoc'");
        line("from javadoc.destinationDir");
        endBlock();
    }

    private void taskSourcesJar() {
        startBlock("task sourcesJar(type: Jar, dependsOn: classes)");
        line("classifier = 'sources'");
        line("from sourceSets.main.allSource");
        endBlock();
    }

    private void line(String content) {
        int padding = indent * indentSpaces;
        StringBuilder sb = new StringBuilder(padding);
        for (int i = 0; i < padding; i++) {
            sb.append(' ');
        }
        sb.append(content);
        contents.add(sb.toString());
    }

    private void startBlock(String blockName) {
        contents.add("");
        line(blockName + " {");
        indent++;
    }
}