package uk.q3c.gitplus.git;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.github.GHIssue;
import org.slf4j.Logger;
import uk.q3c.gitplus.origin.OriginServiceApi;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
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
    private static final String tokenSplitChars = " \t\n\r\f,.:;*?`![]'";
    private static Logger log = getLogger(GitCommit.class);
    private final List<GHIssue> fixReferences;

    private String fullMessage;
    private RevCommit revCommit;
    private String shortMessage;

    public GitCommit(@Nonnull RevCommit revCommit) {
        this.revCommit = revCommit;
        fixReferences = new ArrayList<>();
    }

    public GitCommit(@Nonnull String fullMessage) {
        checkNotNull(fullMessage);
        this.fullMessage = fullMessage;
        fixReferences = new ArrayList<>();
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public List<GHIssue> getFixReferences() {
        return fixReferences;
    }

    public String getFullMessage() {
        if (fullMessage == null) {
            fullMessage = revCommit.getFullMessage();
        }
        return fullMessage;
    }

    public String extractShortMessage() {
        return fullMessage.split("\n")[0];
    }

    public void extractIssueReferences(@Nonnull OriginServiceApi originServiceApi) {
        checkNotNull(originServiceApi);
        fullMessage = correctCommonTypos(getFullMessage());
        shortMessage = extractShortMessage();

        StrTokenizer tokenizer = new StrTokenizer(fullMessage, StrMatcher.charSetMatcher(tokenSplitChars));
        List<String> tokens = tokenizer.getTokenList();
        List<String> expandedTokens = new ArrayList<>();
        String previousToken;
        String currentToken = null;
        for (String token : tokens) {
            previousToken = currentToken;
            currentToken = token;
            String expandedReference = expandIssueReferences(previousToken, currentToken, originServiceApi);
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


    private String expandIssueReferences(String previousToken, String currentToken, OriginServiceApi originServiceApi) {
        if (!currentToken.contains("#") || currentToken.length() < 2) {
            return currentToken;
        }


        String repoName;
        String issueNumberStr;

        if (currentToken.startsWith("#")) {
            int index = currentToken.indexOf('#');
            issueNumberStr = currentToken.substring(index + 1);
            repoName = originServiceApi.getRepoName();
        } else {
            String[] s = currentToken.split("#");
            //Whatever contains the '#' it is not a valid reference
            if (s.length < 2) {
                return currentToken;
            }
            repoName = s[0];
            issueNumberStr = s[1];
        }

        //RepoName obviously invalid
        if (repoName == null || (!repoName.contains("/"))) {
            return currentToken;
        }

        int issueNumber;
        try {
            issueNumber = Integer.parseInt(issueNumberStr);
        } catch (NumberFormatException nfe) {
            log.warn("{} is not a valid issue number", issueNumberStr);
            return currentToken;
        }

        return captureFixesAndExpandReference(originServiceApi, repoName, issueNumber, previousToken, currentToken);

    }

    private String captureFixesAndExpandReference(OriginServiceApi originServiceApi, String repoName, int issueNumber, String previousToken, String
            currentToken) {
        try {
            GHIssue issue = originServiceApi.getIssue(repoName, issueNumber);
            //remove issue reference from short message
            shortMessage = shortMessage.replaceFirst(previousToken + " " + currentToken, "");
            shortMessage = StringUtils.stripStart(shortMessage, tokenSplitChars);
            if (originServiceApi.isIssueFixWord(previousToken)) {
                fixReferences.add(issue);
            }
            return expandedIssue(issue);
        } catch (IOException e) {
            log.warn("Issue {} not found in repo {}", issueNumber, repoName, e);
            return currentToken;
        }
    }

    private String expandedIssue(GHIssue issue) {
        return "[" + issue.getNumber() + "](" + issue.getHtmlUrl() + ")";
    }


    /**
     * Puts in missing space before the '#' where previous word is a key word
     */


    private String correctCommonTypos(String original) {
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
}
