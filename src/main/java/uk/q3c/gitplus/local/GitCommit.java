package uk.q3c.gitplus.local;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import uk.q3c.gitplus.changelog.ChangeLogConfiguration;
import uk.q3c.gitplus.remote.GPIssue;
import uk.q3c.gitplus.remote.GitRemote;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Captures commit information.  Fixes are extracted from commit comments and stored as instances of {@link GHIssue} if other provideds (BitBucket etc) are
 * implemented, their issues would need to be mapped to {@link GHIssue}
 * <p>
 * Created by David Sowerby on 08 Mar 2016
 */
public class GitCommit {
    private static final String TOKEN_SPLIT_CHARS = " \t\n\r\f,.:;*?`![]'";
    private static Logger log = getLogger(GitCommit.class);
    private final List<GPIssue> fixReferences;

    private String fullMessage;
    private String shortMessage;
    private String hash;
    private PersonIdent author;
    private PersonIdent committer;
    private ZonedDateTime commitDate;
    private ZonedDateTime authorDate;

    public GitCommit(@Nonnull String fullMessage) {
        checkNotNull(fullMessage);
        this.fullMessage = fullMessage;
        fixReferences = new ArrayList<>();
    }

    /**
     * Make sure you have called RevWalk.parseBody on {@code commit} before passing to this constructor
     *
     * @param revCommit the RevCommit to copy information from
     */
    public GitCommit(@Nonnull RevCommit revCommit) {
        checkNotNull(revCommit);
        fixReferences = new ArrayList<>();
        setHash(revCommit.getName());
        setCommitter(revCommit.getCommitterIdent());
        setAuthor(revCommit.getAuthorIdent());
        fullMessage = revCommit.getFullMessage();
        hash = revCommit.getId()
                        .getName();
    }

    public ZonedDateTime getAuthorDate() {
        return authorDate;
    }

    public ZonedDateTime getCommitDate() {
        return commitDate;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public List<GPIssue> getIssueReferences() {
        return fixReferences;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public String extractShortMessage() {
        return fullMessage.split("\n")[0];
    }

    public void extractIssueReferences(@Nonnull GitRemote gitRemote) {
        checkNotNull(gitRemote);
        fullMessage = correctCommonTypos(getFullMessage());
        shortMessage = extractShortMessage();

        StrTokenizer tokenizer = new StrTokenizer(fullMessage, StrMatcher.charSetMatcher(TOKEN_SPLIT_CHARS));
        List<String> tokens = tokenizer.getTokenList();
        List<String> expandedTokens = new ArrayList<>();
        String previousToken;
        String currentToken = null;
        for (String token : tokens) {
            previousToken = currentToken;
            currentToken = token;
            String expandedReference = expandIssueReferences(previousToken, currentToken, gitRemote);
            expandedTokens.add(expandedReference);
        }
        final Iterator<String> tokensIterator = tokens.iterator();
        final Iterator<String> expandedTokensIterator = expandedTokens.iterator();

        while (tokensIterator.hasNext()) {
            String token = tokensIterator.next();
            String expandedToken = expandedTokensIterator.next();
            if (!token.equals(expandedToken)) {
                fullMessage = fullMessage.replaceFirst(token, expandedToken);
            }
        }
    }

    private String expandIssueReferences(String previousToken, String currentToken, GitRemote gitRemote) {
        if (!currentToken.contains("#") || currentToken.length() < 2) {
            return currentToken;
        }

        String[] s = currentToken.split("#");
        if (s.length != 2) {
            return currentToken;
        }
        String fullRepoName = s[0];
        String issueNumberStr = s[1];

        int issueNumber;
        try {
            issueNumber = Integer.parseInt(issueNumberStr);
        } catch (NumberFormatException nfe) {
            log.warn("{} is not a valid issue number", issueNumberStr);
            return currentToken;
        }

        return captureFixesAndExpandReference(gitRemote, fullRepoName, issueNumber, previousToken, currentToken);

    }

    private String captureFixesAndExpandReference(GitRemote gitRemote, String fullRepoName, int issueNumber, String previousToken, String
            currentToken) {
        GPIssue gpIssue;
        try {
            if (fullRepoName == null || fullRepoName.isEmpty()) {
                gpIssue = gitRemote.getIssue(issueNumber);
            } else {
                String[] splitRepoName = fullRepoName.split("/");
                gpIssue = gitRemote.getIssue(splitRepoName[0], splitRepoName[1], issueNumber);
            }

            //remove issue reference from short message
            shortMessage = shortMessage.replaceFirst(previousToken + " " + currentToken, "");
            shortMessage = StringUtils.stripStart(shortMessage, TOKEN_SPLIT_CHARS);
            if (gitRemote.isIssueFixWord(previousToken)) {
                fixReferences.add(gpIssue);
            }
            return expandedIssue(gpIssue);
        } catch (Exception e) {
            log.warn("Issue {} not found in repo {}", issueNumber, fullRepoName, e);
            return currentToken;
        }
    }

    private String expandedIssue(GPIssue gpIssue) {
        return "[" + gpIssue.getNumber() + "](" + gpIssue.getHtmlUrl() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GitCommit gitCommit = (GitCommit) o;

        return hash.equals(gitCommit.hash);

    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }

    /**
     * Puts in missing space before the '#' where previous word is a key word
     */


    private String correctCommonTypos(@Nonnull String original) {
        checkNotNull(original);
        return original.replace("Fix#", "Fix #")
                       .replace("fix#", "fix #")
                       .replace("Fixes#", "Fixes #")
                       .replace("fixes#", "fixes #")
                       .replace("See#", "See #")
                       .replace("see#", "see #")

                       .replace("Close#", "Close #")
                       .replace("close#", "close #")
                       .replace("Closes#", "Closes #")
                       .replace("closes#", "closes #")
                       .replace("Resolve#", "Resolve #")
                       .replace("resolve#", "resolve #")
                       .replace("Resolves#", "Resolves #")
                       .replace("resolves#", "resolves #");

    }

    public PersonIdent getAuthor() {
        return author;
    }

    public void setAuthor(@Nonnull PersonIdent author) {
        checkNotNull(author);
        this.author = author;
        authorDate = identToDate(author);
    }

    public PersonIdent getCommitter() {
        return committer;
    }

    public void setCommitter(@Nonnull PersonIdent committer) {
        checkNotNull(committer);
        this.committer = committer;
        commitDate = identToDate(committer);
    }

    private ZonedDateTime identToDate(PersonIdent personIdent) {
        Date when = personIdent.getWhen();
        return when.toInstant()
                   .atZone(personIdent.getTimeZone()
                                      .toZoneId());
    }

    /**
     * Looks for any occurrence of an exclusion tag in {@link #fullMessage}, and returns true if it finds one, otherwise returns false
     *
     * @param changeLogConfiguration the configuration for the change log, which defines the exclusion tags and the opening and closing tag markers
     * @return true if an exclusion tag is found
     */
    public boolean excludedFromChangeLog(ChangeLogConfiguration changeLogConfiguration) {
        for (String exclusionTag : changeLogConfiguration.getExclusionTags()) {
            String fullTag = changeLogConfiguration.getExclusionTagOpen() + exclusionTag + changeLogConfiguration.getExclusionTagClose();
            if (fullMessage.contains(fullTag)) {
                return true;
            }
        }
        return false;
    }
}
