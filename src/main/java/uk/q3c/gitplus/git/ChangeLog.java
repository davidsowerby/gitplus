package uk.q3c.gitplus.git;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.generic.DateTool;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import uk.q3c.gitplus.origin.OriginServiceApi;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by David Sowerby on 07 Mar 2016
 */
public class ChangeLog {
    private static Logger log = getLogger(ChangeLog.class);
    private final VelocityEngine velocityEngine;
    private final VelocityContext velocityContext;
    private final Template velocityTemplate;
    private GitHandler gitHandler;
    private String changeLogFileName = "changelog.md";
    private OriginServiceApi originServiceApi;
    private String projectName;
    private File projectDir;


    public ChangeLog() {
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
        velocityTemplate = velocityEngine.getTemplate("changelog.vm");
        velocityContext = new VelocityContext();
    }

    public void setApiToken(@Nonnull String apiToken) {
        checkNotNull(apiToken);
        originServiceApi.setApiToken(apiToken);
    }

    public void setRepoName(@Nonnull String repoName) {
        checkNotNull(repoName);
        originServiceApi.setRepoName(repoName);
    }

    public File getProjectDir() {
        checkNotNull(projectDir);
        return projectDir;
    }

    public void setProjectDir(@Nonnull File projectDir) {
        checkNotNull(projectDir);
        this.projectDir = projectDir;
    }

    public String getProjectName() {
        if (projectName == null) {
            setProjectName(getGitHandler().getOriginRepoName());
        }
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public GitHandler getGitHandler() {
        return gitHandler;
    }

    public void setGitHandler(GitHandler gitHandler) {
        this.gitHandler = gitHandler;
    }

    public String getChangeLogFileName() {
        return changeLogFileName;
    }

    public void setChangeLogFileName(String changeLogFileName) {
        this.changeLogFileName = changeLogFileName;
    }

    public Template getVelocityTemplate() {
        return velocityTemplate;
    }

    public void createChangeLog() throws IOException {
        File targetFile = defaultTargetFile();
        List<VersionRecord> versionRecordList = getVersionRecords();
        ImmutableList<VersionRecord> versionRecords = ImmutableList.copyOf(versionRecordList)
                                                                   .reverse();
        Repository repo = gitHandler.openRepository();
        RevWalk walk = new RevWalk(repo);

        versionRecords.forEach(vr -> {
            try {
                vr.parse(walk, getOriginServiceApi());
            } catch (IOException e) {
                log.error("Failed to parse a version record", e);
            }
        });
        velocityContext.put("projectName", getProjectName());
        velocityContext.put("versionRecords", versionRecords);
        velocityContext.put("baseUrl", gitHandler.getOriginRepoBaseUrl());
        velocityContext.put("tagUrl", gitHandler.getOriginRepoTagUrl());
        velocityContext.put("dateTool", new DateTool());


        StringWriter w = new StringWriter();
        velocityTemplate.merge(velocityContext, w);
        FileUtils.writeStringToFile(targetFile, w.toString());

        System.out.println(w);
    }

    private File defaultTargetFile() {
        return new File(gitHandler.getProjectDir(), changeLogFileName);

    }

    public OriginServiceApi getOriginServiceApi() {
        return originServiceApi;
    }

    public void setOriginServiceApi(OriginServiceApi originServiceApi) {
        this.originServiceApi = originServiceApi;
    }

    /**
     * Returns a list of {@link VersionRecord}, a small subset of commit information for each commit which is tagged
     *
     * @return a list of {@link VersionRecord}, in order of oldest first
     */
    public List<VersionRecord> getVersionRecords() {
        Repository repo = null;
        List<Ref> tags = gitHandler.getTags();
        List<VersionRecord> versionRecords = new ArrayList<>();
        try {
            repo = gitHandler.openRepository();
            RevWalk walk = new RevWalk(repo);
            for (Ref tag : tags) {
                RevObject revObject = walk.parseAny(tag.getObjectId());
                String tagName = tag.getName()
                                    .replace("refs/tags/", "");
                RevCommit tagCommit;
                LocalDateTime releaseDate = null;

                //annotated tag
                if (revObject instanceof RevTag) {
                    RevTag revTag = (RevTag) revObject;
                    releaseDate = revTag.getTaggerIdent()
                                        .getWhen()
                                        .toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                    tagCommit = (RevCommit) revTag.getObject();

                    //lightweight tag
                } else if (revObject instanceof RevCommit) {
                    tagCommit = (RevCommit) revObject;

                } else {
                    throw new GitHandlerException("Unexpected type while parsing tags");
                }

                LocalDateTime commitDate = LocalDateTime.ofEpochSecond(tagCommit.getCommitTime(), 0, ZoneOffset.UTC);
                versionRecords.add(new VersionRecord(tagName, releaseDate, commitDate, tagCommit));
            }

        } catch (Exception e) {
            throw new GitHandlerException("Unable to read tags " + projectDir.getAbsolutePath(), e);
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
        populateVersionRecords(ImmutableList.copyOf(versionRecords)
                                            .reverse());
        return versionRecords;
    }

    public void populateVersionRecords(List<VersionRecord> versionRecords) {
        Set<RevCommit> commits = gitHandler.extractDevelopCommits();
        Iterator<VersionRecord> versionIterator = versionRecords.iterator();
        VersionRecord currentRecord = versionIterator.next();
        final Iterator<RevCommit> commitIterator = commits.iterator();
        RevCommit currentCommit = null;
        //scroll through commits until first tagged one found (it should be the first commit, but you never know)
        boolean foundStart = false;
        while (commitIterator.hasNext()) {
            currentCommit = commitIterator.next();
            if (currentCommit.equals(currentRecord.getTagCommit())) {
                foundStart = true;
                break;
            }
        }
        if (!foundStart) {
            throw new GitHandlerException("Unable to find first version tag");
        }

        while (commitIterator.hasNext()) {
            VersionRecord nextRecord;
            if (versionIterator.hasNext()) {
                nextRecord = versionIterator.next();
            } else {
                nextRecord = null;
            }
            loadCommits(commitIterator, currentRecord, nextRecord);
            currentRecord = nextRecord;
        }


    }

    /**
     * Loads commits until next tag is found then returns
     *
     * @param commitIterator running iterator keeping track of commit list
     * @param currentRecord  the current VersionRecord
     */
    //This includes the tagged commit again - that just makes it easier to process
    private void loadCommits(Iterator<RevCommit> commitIterator, VersionRecord currentRecord, VersionRecord nextRecord) {
        //having the tag commit in the list as well makes it easier to process
        currentRecord.addCommit(currentRecord.getTagCommit());
        while (commitIterator.hasNext()) {
            RevCommit nextCommit = commitIterator.next();
            //no more records, just add remaining commits to the last record
            if (nextRecord == null) {
                currentRecord.addCommit(nextCommit);
            } else {
                if (nextCommit.equals(nextRecord.getTagCommit())) {
                    break;
                } else {
                    currentRecord.addCommit(nextCommit);
                }
            }
        }
    }

}
