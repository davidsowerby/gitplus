package uk.q3c.gitplus.changelog

import org.apache.velocity.exception.ResourceNotFoundException
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.gitplus.gitplus.GitPlus
import uk.q3c.gitplus.gitplus.GitPlusConfiguration
import uk.q3c.gitplus.local.GitLocal
import uk.q3c.gitplus.util.UserHomeBuildPropertiesLoader

/**
 * Created by David Sowerby on 07 Mar 2016
 */
class ChangeLogTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    ChangeLog changeLog
    ChangeLogConfiguration changeLogConfiguration
    GitPlus gitPlus

    def setup() {
        temp = temporaryFolder.getRoot()
        File projectDir = new File("/home/david/git/q3c-testUtil")
        String apiToken = new UserHomeBuildPropertiesLoader().load().githubKeyRestricted()
        GitPlusConfiguration gitPlusConfiguration = new GitPlusConfiguration().projectDir(projectDir).apiToken(apiToken).remoteRepoFullName('davidsowerby/q3c-testUtil')
        gitPlus = new GitPlus(gitPlusConfiguration, new GitLocal(gitPlusConfiguration))
        changeLogConfiguration = new ChangeLogConfiguration()

    }

    def "run"() {
        given:
        File outputFile = new File(temp, 'changelog.md')
        changeLogConfiguration.outputFile(outputFile)
        changeLog = new ChangeLog(gitPlus, changeLogConfiguration)


        when:
        changeLog.createChangeLog()

        then:
        outputFile.exists()
    }

    def "constructor validates configuration"() {
        given:
        ChangeLogConfiguration changeLogConfiguration = Mock(ChangeLogConfiguration)
        GitPlus gitPlus = Mock(GitPlus)

        when:
        new ChangeLog(gitPlus, changeLogConfiguration)

        then:
        1 * changeLogConfiguration.validate()
        thrown ResourceNotFoundException //outputFile not set
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

//could test using a clone from a krail project
//don;t scrollthrough to first version as now - the most recent commit will not get tagged until it is released