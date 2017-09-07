package uk.q3c.build.gitplus.test

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.StoredConfig
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.DefaultGitLocalConfiguration
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.local.GitLocalConfiguration
import uk.q3c.build.gitplus.remote.DefaultGitRemoteConfiguration
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration

/**
 * A Mockito mock for [GitPlus]. [GitLocal] and [GitRemote] are returned as mocks, [GitLocalConfiguration] and
 * [GitRemoteConfiguration] as default instances
 *
 * Created by David Sowerby on 03 Sep 2017
 */
fun mockGitPlusWithDataConfig(): GitPlus {
    val gitPlus = mockGitPlus()
    configureLocalWithDataConfig(gitPlus.local)
    val remoteConfiguration = configureRemoteWithDataConfig(gitPlus.remote)
    `when`(gitPlus.remote.repoName).thenReturn(remoteConfiguration.repoName)
    `when`(gitPlus.remote.repoUser).thenReturn(remoteConfiguration.repoUser)
    return gitPlus
}

/**
 * A Mockito mock for [GitPlus]. [GitLocal] and [GitRemote] are returned as mocks, [GitLocalConfiguration] and
 * [GitRemoteConfiguration] as mocks
 *
 * Created by David Sowerby on 03 Sep 2017
 */
fun mockGitPlusWithMockConfig(): GitPlus {
    val gitPlus = mockGitPlus()
    configureLocalWithMockConfig(gitPlus.local)
    configureRemoteWithMockConfig(gitPlus.remote)
    return gitPlus

}

/**
 * A [GitLocal] mock with [GitLocalConfiguration] as default instance
 */
fun mockGitLocalWithDataConfig(): GitLocal {
    val gitLocal = mock(GitLocal::class.java)
    return configureLocalWithDataConfig(gitLocal)
}

/**
 * A [GitLocal] mock with [GitLocalConfiguration] as mock
 */
fun mockGitLocalWithMockConfig(): GitLocal {
    val gitLocal = mock(GitLocal::class.java)
    return configureLocalWithMockConfig(gitLocal)
}

/**
 * A [GitRemote] mock with [GitRemoteConfiguration] as default instance
 */
fun mockGitRemoteWithDataConfig(): GitRemote {
    val gitRemote = mock(GitRemote::class.java)
    configureRemoteWithDataConfig(gitRemote)
    return gitRemote
}

/**
 * A [GitRemote] mock with [GitRemoteConfiguration] as mock
 */
fun mockGitRemoteWithMockConfig(): GitRemote {
    val gitRemote = mock(GitRemote::class.java)
    configureRemoteWithMockConfig(gitRemote)
    return gitRemote
}

/**
 * Returns a Mockito mock for JGit Git, with a mocked repository and stored config
 */
fun mockGit(): Git {
    val git = mock(Git::class.java)
    val repository = mock(Repository::class.java)
    val storedConfig = mock(StoredConfig::class.java)
    `when`(git.repository).thenReturn(repository)
    `when`(repository.config).thenReturn(storedConfig)
    return git
}

private fun configureLocalWithDataConfig(gitLocal: GitLocal): GitLocal {
    val gitLocalConfiguration = DefaultGitLocalConfiguration()
    `when`(gitLocal.configuration).thenReturn(gitLocalConfiguration)
    return gitLocal
}

private fun configureRemoteWithDataConfig(gitRemote: GitRemote): GitRemoteConfiguration {
    val configuration = DefaultGitRemoteConfiguration()
    `when`(gitRemote.configuration).thenReturn(configuration)
    return configuration
}

private fun configureLocalWithMockConfig(gitLocal: GitLocal): GitLocal {
    val gitLocalConfiguration = mock(GitLocalConfiguration::class.java)
    `when`(gitLocal.configuration).thenReturn(gitLocalConfiguration)
    return gitLocal
}

private fun configureRemoteWithMockConfig(gitRemote: GitRemote): GitRemoteConfiguration {
    val configuration = mock(GitRemoteConfiguration::class.java)
    `when`(gitRemote.configuration).thenReturn(configuration)
    return configuration
}


/**
 * A Mockito GitPlus with local and remote mocks, but no configuration.
 * [mockGitPlusWithMockConfig] and [mockGitPlusWithDataConfig] are probably more useful
 */
fun mockGitPlus(): GitPlus {
    val gitPlus = mock(GitPlus::class.java)
    val gitLocal = mock(GitLocal::class.java)
    val gitRemote = mock(GitRemote::class.java)
    `when`(gitPlus.local).thenReturn(gitLocal)
    `when`(gitPlus.remote).thenReturn(gitRemote)
    return gitPlus

}

