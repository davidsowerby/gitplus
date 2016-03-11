package uk.q3c.gitplus.git;

import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.RebaseTodoLine;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

import static org.eclipse.jgit.lib.RebaseTodoLine.Action.FIXUP;

/**
 * A Rebase handler specifically to apply a Git FIXUP to the last commit (therefore merging the last two commits, but
 * using the comment from the earlier commit)
 * Created by David Sowerby on 18/01/15.
 */
public class RebaseFixupLastCommit implements RebaseCommand.InteractiveHandler {
    private RevCommit headCommit;

    public RebaseFixupLastCommit(RevCommit headCommit) {
        this.headCommit = headCommit;
    }

    public void prepareSteps(List<RebaseTodoLine> steps) {
        AbbreviatedObjectId abbrevId = AbbreviatedObjectId.fromObjectId(headCommit);
        RebaseTodoLine line = new RebaseTodoLine(FIXUP, abbrevId, "not used");
        steps.add(line);
    }

    public String modifyCommitMessage(String oldMessage) {
        return oldMessage;
    }
}
