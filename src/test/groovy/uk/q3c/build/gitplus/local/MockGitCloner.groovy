package uk.q3c.build.gitplus.local

import org.jetbrains.annotations.NotNull

/**
 * Created by David Sowerby on 17 May 2017
 */
class MockGitCloner implements GitCloner {
    boolean cloned = false
    File localDir
    String remoteUrl

    @Override
    void doClone(@NotNull File localDir, @NotNull String remoteUrl, @NotNull GitInitChecker gitInitChecker) {
        this.localDir = localDir
        this.remoteUrl = remoteUrl
        cloned = true
        gitInitChecker.initDone()
    }
}
