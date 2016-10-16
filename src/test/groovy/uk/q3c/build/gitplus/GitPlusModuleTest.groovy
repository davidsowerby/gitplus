package uk.q3c.build.gitplus

import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Provider
import spock.lang.Specification
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.ServiceProvider

/**
 * Created by David Sowerby on 31 Oct 2016
 */
class GitPlusModuleTest extends Specification {

    static class TestClass {

        Map<ServiceProvider, Provider<GitRemote>> map

        @Inject
        public TestClass(Map<ServiceProvider, Provider<GitRemote>> map) {
            this.map = map
        }

    }


    def "configure"() {
        given:
        Injector injector = Guice.createInjector(new GitPlusModule())

        expect:
        TestClass testClass = injector.getInstance(TestClass)
        testClass.map.size() == 1
        testClass.map.get(ServiceProvider.GITHUB) != null


    }
}
