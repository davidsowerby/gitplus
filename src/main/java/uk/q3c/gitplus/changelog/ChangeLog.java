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
        File outputFile = configuration.getOutputFile();
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
        FileUtils.writeStringToFile(outputFile, w.toString());

        System.out.println(w);
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
        Set<GitCommit> commits = gitPlus.extractDevelopCommits();
        Iterator<GitCommit> commitIterator = commits.iterator();
        GitCommit firstCommit = commitIterator.next();
        //If constructing changelog for a released version, most recent commit has a tag
        //if not yet released use 'current build' pseudo tag for most recent commit
        Optional<Tag> tagForFirstsCommit = tagForCommit(firstCommit);
        Tag tag = tagForFirstsCommit.isPresent() ? tagForFirstsCommit.get() : currentBuildTag(firstCommit);
        VersionRecord currentVersionRecord = new VersionRecord(tag, configuration);
        currentVersionRecord.addCommit(firstCommit);
        versionRecords.add(currentVersionRecord);

        while (commitIterator.hasNext()) {
            GitCommit currentCommit = commitIterator.next();
            Optional<Tag> tagForCommit = tagForCommit(currentCommit);
            if (tagForCommit.isPresent()) {
                currentVersionRecord = new VersionRecord(tagForCommit.get(), configuration);
                versionRecords.add(currentVersionRecord);
            }
            currentVersionRecord.addCommit(currentCommit);
        }
    }

    private Tag currentBuildTag(GitCommit commit) {
        return new Tag("current build")
                .tagType(Tag.TagType.PSEUDO)
                .releaseDate(commit.getCommitDate())
                .fullMessage("Pseudo tag on latest commit")
                .taggerIdent(commit.getCommitter());
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
