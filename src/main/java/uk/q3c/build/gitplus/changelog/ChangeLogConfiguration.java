package uk.q3c.build.gitplus.changelog;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.q3c.build.gitplus.changelog.ChangeLogConfiguration.OutputTarget.USE_FILE_SPEC;

/**
 * A execute object for {@link ChangeLog}
 * <p>
 * Created by David Sowerby on 13 Mar 2016
 */

public class ChangeLogConfiguration {

    /**
     * The {@link #outputFilename} is referenced to an OutputTarget directory, unless USE_FILE_SPEC is selected, in which case the {@link #outputFile} must
     * be specified
     */
    public enum OutputTarget {
        PROJECT_ROOT, PROJECT_BUILD_ROOT, WIKI_ROOT, CURRENT_DIR, USE_FILE_SPEC
    }

    public static final String LATEST_COMMIT = "latest commit";
    public static final String LATEST_VERSION = "latest version";
    public static final String DEFAULT_PULL_REQUESTS_TITLE = "Pull Requests";
    public static final ImmutableMap<String, String> defaultTypoMap = new ImmutableMap.Builder<String, String>().put("Fix#", "Fix #")
                                                                                                                .put("fix#", "fix #")
                                                                                                                .put("Fixes#", "Fixes #")
                                                                                                                .put("fixes#", "fixes #")
                                                                                                                .put("See#", "See #")
                                                                                                                .put("see#", "see #")
                                                                                                                .put("Close#", "Close #")
                                                                                                                .put("close#", "close #")
                                                                                                                .put("Closes#", "Closes #")
                                                                                                                .put("closes#", "closes #")
                                                                                                                .put("Resolve#", "Resolve #")
                                                                                                                .put("resolve#", "resolve #")
                                                                                                                .put("Resolves#", "Resolves #")
                                                                                                                .put("resolves#", "resolves #")
                                                                                                                .build();
    @SuppressWarnings("WeakerAccess")
    public static final ImmutableSet<String> defaultFixSet = ImmutableSet.of("bug");
    @SuppressWarnings("WeakerAccess")
    public static final ImmutableSet<String> defaultEnhancementSet = ImmutableSet.of("enhancement", "performance");
    @SuppressWarnings("WeakerAccess")
    public static final ImmutableSet<String> defaultDocumentationSet = ImmutableSet.of("documentation");
    @SuppressWarnings("WeakerAccess")
    public static final ImmutableSet<String> defaultTaskSet = ImmutableSet.of("task");
    @SuppressWarnings("WeakerAccess")
    public static final ImmutableSet<String> defaultQualitySet = ImmutableSet.of("testing", "quality");


    //    @formatter:off
    public static  final ImmutableMap<String, Set<String>> defaultLabelGroups = new ImmutableMap.Builder<String, Set<String>>()
            .put(DEFAULT_PULL_REQUESTS_TITLE,ImmutableSet.of()) // pull requests do not need mapping
            .put("Fixes", defaultFixSet)
            .put("Quality",defaultQualitySet)
            .put("Enhancements",defaultEnhancementSet)
            .put("Tasks",defaultTaskSet)
            .put("Documentation",defaultDocumentationSet)
            .build();

    //    @formatter:on

    public static final String DEFAULT_TEMPLATE = "markdown.vm";
    private String templateName = DEFAULT_TEMPLATE;
    private Map<String, Set<String>> labelGroups = defaultLabelGroups;
    private String exclusionTagOpen = "{{";
    private String exclusionTagClose = "}}";
    private boolean separatePullRequests = true;
    private Set<String> exclusionTags = ImmutableSet.of("javadoc");

    private Map<String, String> typoMap = defaultTypoMap;
    private boolean correctTypos = true;
    private String outputFilename = "changelog.md";
    private String pullRequestTitle = DEFAULT_PULL_REQUESTS_TITLE;
    private OutputTarget outputDirectory = OutputTarget.WIKI_ROOT;
    private File outputFile;
    private String fromVersion = LATEST_COMMIT;
    private String toVersion = null;
    private int numberOfVersions = 0;

    public ChangeLogConfiguration fromVersion(final String fromVersion) {
        this.fromVersion = fromVersion;
        return this;
    }

    public ChangeLogConfiguration toVersion(final String toVersion) {
        this.toVersion = toVersion;
        return this;
    }

    public ChangeLogConfiguration numberOfVersions(final int numberOfVersions) {
        this.numberOfVersions = numberOfVersions;
        return this;
    }

    public String getFromVersion() {
        return fromVersion;
    }

    public String getToVersion() {
        return toVersion;
    }

    public int getNumberOfVersions() {
        return numberOfVersions;
    }

    public ChangeLogConfiguration outputFile(final File outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    public File getOutputFile() {
        return outputFile;
    }

    /**
     * The {@link #outputFilename} is referenced to an OutputTarget directory, unless USE_FILE_SPEC is selected, in which case the {@link #outputFile} must
     * be specified
     */
    public ChangeLogConfiguration outputDirectory(final OutputTarget outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public OutputTarget getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * The heading used to describe pull requests in the output file
     *
     * @param pullRequestTitle the title to use
     * @return this for fluency
     */
    public ChangeLogConfiguration pullRequestTitle(final String pullRequestTitle) {
        this.pullRequestTitle = pullRequestTitle;
        return this;
    }

    public String getPullRequestTitle() {
        return pullRequestTitle;
    }

    public boolean isCorrectTypos() {
        return correctTypos;
    }


    public String getTemplateName() {
        return templateName;
    }


    /**
     * @return labelGroups a mapping of label groups to labels.  Uses a {@link LinkedHashMap} to retain insertion order.
     */
    public Map<String, Set<String>> getLabelGroups() {
        return labelGroups;
    }


    public String getExclusionTagOpen() {
        return exclusionTagOpen;
    }


    public String getExclusionTagClose() {
        return exclusionTagClose;
    }


    public boolean isSeparatePullRequests() {
        return separatePullRequests;
    }


    public Set<String> getExclusionTags() {
        return exclusionTags;
    }


    public Map<String, String> getTypoMap() {
        return typoMap;
    }


    public String getOutputFilename() {
        return outputFilename;
    }

    public ChangeLogConfiguration templateName(final String templateName) {
        this.templateName = templateName;
        return this;
    }

    /**
     * Set the way in which issues are grouped together by label.  Issues with multiple labels may be included in the {@link ChangeLog} output multiple times.
     * Issues which are not labelled, or only have a label that does not appear in this setting, will not be included in the {@link ChangeLog} output
     *
     * @param labelGroups a mapping of label groups to labels.  A {@link LinkedHashMap} or {@link ImmutableMap} is advised, in order to retain insertion order.
     */
    public ChangeLogConfiguration labelGroups(final Map<String, Set<String>> labelGroups) {
        this.labelGroups = labelGroups;
        return this;
    }

    /**
     * An exclusionTag stops a commit message from being processed into the Change Log.  This sets the marker which opens the tag (default is '{{')
     *
     * @param messageTagOpen the closing marker for an exclusion tag
     * @return this for fluency
     */
    public ChangeLogConfiguration exclusionTagOpen(final String messageTagOpen) {
        this.exclusionTagOpen = messageTagOpen;
        return this;
    }

    /**
     * An exclusionTag stops a commit message from being processed into the Change Log.  This sets the marker which closes the tag (default is '}}')
     *
     * @param messageTagClose the closing marker for an exclusion tag
     * @return this for fluency
     */
    public ChangeLogConfiguration exclusionTagClose(final String messageTagClose) {
        this.exclusionTagClose = messageTagClose;
        return this;
    }

    /**
     * If true, pull requests are extracted under their own heading in the change log.  If false, they are merged with other issues according to the labels
     * attached to them
     *
     * @param separatePullRequests true to extract pull requests separately
     * @return this for fluency
     */
    public ChangeLogConfiguration separatePullRequests(final boolean separatePullRequests) {
        this.separatePullRequests = separatePullRequests;
        return this;
    }

    /**
     * An exclusionTag stops a commit message from being processed into the Change Log.  With default settings, therefore, any commit message containing
     * '{{javadoc}}' is just ignored.  This method defines which tags should be excluded.  See also {@link #exclusionTagOpen} and {@link #exclusionTagClose}
     *
     * @param exclusionTags the tags to be ignored
     * @return this for fluency
     */
    public ChangeLogConfiguration exclusionTags(final Set<String> exclusionTags) {
        this.exclusionTags = exclusionTags;
        return this;
    }

    /**
     * A simple 'search and replace' map of typos.  Usually used to find typos which would affect correct identofocation of related issues
     *
     * @param typoMap A map of search (the key) and replace (the value)
     * @return this for fluency
     */
    public ChangeLogConfiguration typoMap(final Map<String, String> typoMap) {
        this.typoMap = typoMap;
        return this;
    }

    public ChangeLogConfiguration correctTypos(final boolean correctTypos) {
        this.correctTypos = correctTypos;
        return this;
    }

    public ChangeLogConfiguration outputFileName(final String outputFile) {
        this.outputFilename = outputFile;
        return this;
    }


    public void validate() {
        if (outputDirectory != USE_FILE_SPEC && outputFilename == null) {
            throw new ChangeLogConfigurationException("outputFileName must be specified");
        }
        if (outputDirectory == USE_FILE_SPEC && outputFile == null) {
            throw new ChangeLogConfigurationException("When output target is " + USE_FILE_SPEC.name() + " outputFile must be specified");
        }
    }

    public boolean fromLatestCommit() {
        return fromVersion.equals(LATEST_COMMIT);
    }

    public boolean fromLatestVersion() {
        return fromVersion.equals(LATEST_VERSION);
    }

    public boolean isFromVersion(@Nonnull String version) {
        checkNotNull(version);
        return fromVersion != null && fromVersion.equals(version);
    }

    public boolean isToVersion(@Nonnull String version) {
        checkNotNull(version);
        return toVersion != null && toVersion.equals(version);
    }
}
