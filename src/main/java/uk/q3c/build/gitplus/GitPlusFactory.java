package uk.q3c.build.gitplus;

import com.google.inject.Guice;
import com.google.inject.Injector;
import uk.q3c.build.gitplus.gitplus.GitPlus;

/**
 * Created by David Sowerby on 21 Dec 2016
 */
@SuppressWarnings({"StaticVariableUsedBeforeInitialization", "StaticVariableMayNotBeInitialized"})
public class GitPlusFactory {

    private GitPlusFactory() {
    }

    public static GitPlus getInstance() {
        final Injector injector = Guice.createInjector(new GitPlusModule());
        return injector.getInstance(GitPlus.class);
    }
}
