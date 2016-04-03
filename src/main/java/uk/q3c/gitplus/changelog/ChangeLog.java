package uk.q3c.gitplus.changelog;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import uk.q3c.gitplus.gitplus.GitPlus;
import uk.q3c.gitplus.local.GitCommit;
import uk.q3c.gitplus.local.Tag;
import uk.q3c.gitplus.remote.GitRemote;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Builds a list of {@link VersionRecord}.  The versions are identified by tags. Each VersionRecord holds a set of GitCommit instances, which make up a
 * version.  GitCommmit parses commit messages fro issue fix references, and the VersionRecord collates those into groups of issues (for example, 'bug',
 * 'enhancement', 'quality'.  The mapping of issues labels to issue groups is user defined via {@link ChangeLogConfiguration#labelGroups(Map)}.
 * <p>
 * Output format is defined by a Velocity template
 * <p>
 * <p>
 * Created by David Sowerby on 07 Mar 2016
 */
public class ChangeLog {
    public static final String DEFAULT_TEMPLATE = "markdown.vm";
    private static Logger log = getLogger(ChangeLog.class);
    private final VelocityContext velocityContext;
    private final ChangeLogConfiguration configuration;
    private Template velocityTemplate;
    private GitPlus gitPlus;
    private Map<String, Tag> tagMap;
    private ArrayList<VersionRecord> versionRecords;


    public ChangeLog(@Nonnull GitPlus gitPlus, @Nonnull ChangeLogConfiguration configuration) {
        checkNotNull(gitPlus);
        checkNotNull(configuration);
        this.gitPlus = gitPlus;
        this.configuration = configuration;
        configuration.validate();
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
        velocityTemplate = velocityEngine.getTemplate(configuration.getTemplateName());
        velocityContext = new VelocityContext();
        gitPlus.createOrVerifyRepos();
    }


    public String getProjectName() {
        return gitPlus.getProjectName();
    }


    public void createChangeLog() throws IOException {
        assembleVersionRecords();


        versionRecords.forEach(vr -> {
            try {
                vr.parse(getGitRemote());
            } catch (IOException e) {
                log.error("Failed to parse a version record", e);
            }
        });
        velocityContext.put("projectName", getProjectName());
        velocityContext.put("versionRecords", versionRecords);
        velocityContext.put("baseUrl", gitPlus.getRemoteHtmlUrl());
        velocityContext.put("tagUrl", gitPlus.getRemoteTagUrl());
        velocityContext.put("dateTool", new DateTool());


        StringWriter w = new StringWriter();
        velocityTemplate.merge(velocityContext, w);
        FileUtils.writeStringToFile(getOutputFile(), w.toString());

    }

    /**
     * Derives the output file location and name from {@link #configuration}
     *
     * @return the output File
     */
    public File getOutputFile() {
        File outputFile;
        switch (configuration.getOutputDirectory()) {
            case USE_FILE_SPEC:
                outputFile = configuration.getOutputFile();
                break;
            case PROJECT_ROOT:
                outputFile = new File(gitPlus.getGitLocal()
                                             .getProjectDir(), configuration.getOutputFilename());
                break;
            case PROJECT_BUILD_ROOT:
                File buildDir = new File(gitPlus.getGitLocal()
                                                .getProjectDir(), "build");
                outputFile = new File(buildDir, configuration.getOutputFilename());
                break;
            case WIKI_ROOT:
                outputFile = new File(gitPlus.getWikiLocal()
                                             .getProjectDir(), configuration.getOutputFilename());
                break;
            case CURRENT_DIR:
                File currentDir = new File(".");
                outputFile = new File(currentDir, configuration.getOutputFilename());
                break;
            default:
                throw new ChangeLogConfigurationException("Unrecognised output directory, " + configuration.getOutputDirectory()
                                                                                                           .name());

        }
        return outputFile;
    }


    private GitRemote getGitRemote() throws IOException {
        return gitPlus.getGitRemote();
    }


    public List<VersionRecord> getVersionRecords() {
        return versionRecords;
    }

    private void assembleVersionRecords() {
        buildTagMap();
        versionRecords = new ArrayList<>();
        List<GitCommit> commits = gitPlus.extractDevelopCommits();
        int index = scrollToStartCommit(commits);


        //If constructing changelog for a released version, most recent commit has a tag
        //if not yet released use 'current build' pseudo tag for most recent commit
        GitCommit firstCommit = commits.get(index);
        Optional<Tag> tagForFirstCommit = tagForCommit(firstCommit);
        Tag tag = tagForFirstCommit.isPresent() ? tagForFirstCommit.get() : new DefaultCurrentBuildTag(firstCommit);

        VersionRecord currentVersionRecord = new VersionRecord(tag, configuration);
        currentVersionRecord.addCommit(firstCommit);
        versionRecords.add(currentVersionRecord);

        //We need to count versions, but tagCount does not include pseudo tag, as that is not a version
        int tagCount = tag.isPseudoTag() ? 0 : 1;
        boolean parseComplete = false;
        boolean onFinalTag = false;
        while (index < commits.size() - 1 && !parseComplete) {
            index++;
            GitCommit currentCommit = commits.get(index);
            Optional<Tag> tagForCommit = tagForCommit(currentCommit);
            if (tagForCommit.isPresent() && isVersionTag(tagForCommit.get())) {
                if (onFinalTag) {
                    parseComplete = true;
                } else {
                    currentVersionRecord = new VersionRecord(tagForCommit.get(), configuration);
                    versionRecords.add(currentVersionRecord);
                    tagCount++;
                    onFinalTag = isFinalTag(tagForCommit.get(), tagCount);
                    currentVersionRecord.addCommit(currentCommit);
                }
            } else {
                currentVersionRecord.addCommit(currentCommit);
            }
        }

    }

    private boolean isFinalTag(Tag tag, int tagCount) {
        final int numberOfVersions = configuration.getNumberOfVersions();
        return (configuration.isToVersion(tag.getTagName()) || (numberOfVersions > 0 && tagCount >= numberOfVersions));
    }

    /**
     * returns the index to the first selected commit (where selected is determined by the setting of configuration.getFromVersion())
     */
    private int scrollToStartCommit(List<GitCommit> commits) {
        int index = 0;
        GitCommit commit = commits.get(index);

        boolean startFound = false;
        if (configuration.fromLatestCommit()) {
            startFound = true;
        }
        while (!startFound && index < commits.size()) {
            Optional<Tag> tagForCommit = tagForCommit(commit);
            if (tagForCommit.isPresent() && isVersionTag(tagForCommit.get())) {
                if (configuration.fromLatestVersion() || configuration.isFromVersion(tagForCommit.get()
                                                                                                 .getTagName())) {
                    startFound = true;
                } else {
                    index++;
                    commit = commits.get(index);
                }
            } else {
                index++;
                commit = commits.get(index);
            }
        }
        if (!startFound) {
            throw new ChangeLogConfigurationException("Unable to find the 'fromVersion' of " + configuration.getFromVersion());
        }
        return index;
    }

    /**
     * to be replaced by a filter
     *
     * @param tagForCommit
     * @return
     */
    private boolean isVersionTag(Tag tagForCommit) {
        return true;
    }


    private void buildTagMap() {
        List<Tag> tags = gitPlus.getTags();
        tagMap = new HashMap<>();
        tags.forEach(t -> tagMap.put(t.getCommit()
                                      .getHash(), t));
    }

    private Optional<Tag> tagForCommit(GitCommit commit) {
        return tagMap.containsKey(commit.getHash()) ? Optional.of(tagMap.get(commit.getHash())) : Optional.empty();
    }


}
