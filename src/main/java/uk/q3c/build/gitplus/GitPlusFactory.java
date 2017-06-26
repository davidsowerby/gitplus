package uk.q3c.build.gitplus;

import com.google.inject.Guice;
import com.google.inject.Injector;
import uk.q3c.build.gitplus.gitplus.GitPlus;

/**
 * Created by David Sowerby on 21 Dec 2016
 */
@SuppressWarnings("ClassWithOnlyPrivateConstructors")
public class GitPlusFactory {

    private GitPlusFactory() {
        // don't want this used
    }

    public static GitPlus getInstance() {
        final Injector injector = Guice.createInjector(new GitPlusModule());
        return injector.getInstance(GitPlus.class);
    }
}
