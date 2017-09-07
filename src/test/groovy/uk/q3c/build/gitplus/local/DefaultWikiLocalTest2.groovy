package uk.q3c.build.gitplus.local

import org.eclipse.jgit.api.Git
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.remote.GitRemote

/**
 * Created by David Sowerby on 22 Dec 2016
 */
class DefaultWikiLocalTest2 extends Specification {


    WikiLocal wikiLocal
    GitLocal gitLocal = Mock(GitLocal)
    GitLocalConfiguration localConfiguration = Mock(GitLocalConfiguration)
    BranchConfigProvider branchConfigProvider = Mock(BranchConfigProvider)
    GitProvider gitProvider = Mock(GitProvider)
    GitRemote remote = Mock(GitRemote)
    Git git = Mock(Git)
    GitInitChecker mockInitChecker = Mock(GitInitChecker)
    GitCloner cloner = Mock(GitCloner)
    GitPlus gitPlus = Mock(GitPlus)

    def setup() {
        gitPlus.local >> gitLocal
        gitPlus.remote >> remote
        wikiLocal = new DefaultWikiLocal(branchConfigProvider, gitProvider, localConfiguration, mockInitChecker, cloner)
        gitLocal.projectDirParent >> new File('.')
    }

    def "prepare does not validate if not active"() {

        when:
        wikiLocal.prepare(gitPlus)

        then:
        2 * localConfiguration.getActive() >> true  // as super.prepare() called
        1 * gitProvider.openRepository(localConfiguration) >> git
        1 * localConfiguration.validate(remote)

        when:
        wikiLocal.active = false
        wikiLocal.prepare(gitPlus)

        then:
        1 * localConfiguration.getActive() >> false
        0 * localConfiguration.validate(remote)
    }
}
