package uk.q3c.gitplus.changelog;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A execute object for {@link ChangeLog}
 * <p>
 * Created by David Sowerby on 13 Mar 2016
 */

public class ChangeLogConfiguration {
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
    public static final ImmutableSet<String> defaultFixSet = ImmutableSet.of("bug");
    public static final ImmutableSet<String> defaultEnhancementSet = ImmutableSet.of("enhancement", "performance");
    public static final ImmutableSet<String> defaultDocumentationSet = ImmutableSet.of("documentation");
    public static final ImmutableSet<String> defaultTaskSet = ImmutableSet.of("task");
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

    private String templateName = ChangeLog.DEFAULT_TEMPLATE;
    private Map<String, Set<String>> labelGroups = defaultLabelGroups;
    private String messageTagOpen = "{{";
    private String messageTagClose = "}}";
    private boolean separatePullRequests = true;
    private Set<String> excludedMessageTags = new HashSet<>();

    private Map<String, String> typoMap = defaultTypoMap;
    private boolean useTypoMap = true;
    private File outputFile;
    private String pullRequestTitle = DEFAULT_PULL_REQUESTS_TITLE;

    public ChangeLogConfiguration pullRequestTitle(final String pullRequestTitle) {
        this.pullRequestTitle = pullRequestTitle;
        return this;
    }

    public String getPullRequestTitle() {
        return pullRequestTitle;
    }

    public boolean isUseTypoMap() {
        return useTypoMap;
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


    public String getMessageTagOpen() {
        return messageTagOpen;
    }


    public String getMessageTagClose() {
        return messageTagClose;
    }


    public boolean isSeparatePullRequests() {
        return separatePullRequests;
    }


    public Set<String> getExcludedMessageTags() {
        return excludedMessageTags;
    }


    public Map<String, String> getTypoMap() {
        return typoMap;
    }


    public File getOutputFile() {
        return outputFile;
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

    public ChangeLogConfiguration messageTagOpen(final String messageTagOpen) {
        this.messageTagOpen = messageTagOpen;
        return this;
    }

    public ChangeLogConfiguration messageTagClose(final String messageTagClose) {
        this.messageTagClose = messageTagClose;
        return this;
    }

    public ChangeLogConfiguration separatePullRequests(final boolean separatePullRequests) {
        this.separatePullRequests = separatePullRequests;
        return this;
    }

    public ChangeLogConfiguration excludedMessageTags(final Set<String> excludedMessageTags) {
        this.excludedMessageTags = excludedMessageTags;
        return this;
    }

    public ChangeLogConfiguration typoMap(final Map<String, String> typoMap) {
        this.typoMap = typoMap;
        return this;
    }

    public ChangeLogConfiguration useTypoMap(final boolean useTypoMap) {
        this.useTypoMap = useTypoMap;
        return this;
    }

    public ChangeLogConfiguration outputFile(final File outputFile) {
        this.outputFile = outputFile;
        return this;
    }


    public void validate() {
        if (outputFile == null) {
            throw new ChangeLogConfigurationException("outputFile must be specified");
        }
    }
}
