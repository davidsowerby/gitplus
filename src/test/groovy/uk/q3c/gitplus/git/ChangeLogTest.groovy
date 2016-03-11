package uk.q3c.gitplus.git

import spock.lang.Specification
import uk.q3c.gitplus.origin.GitHubServiceApi
import uk.q3c.gitplus.origin.OriginServiceApi
import uk.q3c.gitplus.util.DefaultBuildPropertiesLoader

/**
 * Created by David Sowerby on 07 Mar 2016
 */
class ChangeLogTest extends Specification {

    ChangeLog changeLog

    def setup() {
        File projectDir = new File("/home/david/git/q3c-testUtil")
        GitHandler gitHandler = new GitHandler()
        gitHandler.setProjectDir(projectDir)
        changeLog = new ChangeLog()
        changeLog.setGitHandler(gitHandler)
        OriginServiceApi originServiceApi = new GitHubServiceApi()
        changeLog.setOriginServiceApi(originServiceApi)
        changeLog.setApiToken(new DefaultBuildPropertiesLoader().load().githubKey())
        changeLog.setRepoName("davidsowerby/q3c-testUtil")
    }

    def "run"() {
        when:
        changeLog.createChangeLog()

        then:
        new File("/home/david/git/q3c-testUtil/changelog.md").exists()
    }

//    @Test
//    public void versionRecords() throws Exception {
//        // given
//        createAndAddFile(1);
//        handler.commit( "commit1");
//        handler.tag( "tagga");
//        createAndAddFile(2);
//        handler.commit( "commit2");
//        handler.tag( "taggb");
////        handler.createBranch("develop");
//
//        //when
//        List<VersionRecord> versionRecords = handler.getVersionRecords();
//        ImmutableSet<RevCommit> masterCommits = handler.extractMasterCommits();
//
//        //then
//        assertThat(versionRecords.size()).isEqualTo(2);
//        assertThat(masterCommits).containsOnly(versionRecords.get(0)
//                .getTagCommit(), versionRecords.get(1)
//                .getTagCommit());
//        assertThat(versionRecords.get(0)
//                .getTagName()).isEqualTo("tagga");
//        assertThat(versionRecords.get(1)
//                .getTagName()).isEqualTo("taggb");
//    }
}
