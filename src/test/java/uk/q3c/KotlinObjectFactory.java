package uk.q3c;

import uk.q3c.build.gitplus.creator.gradle.DefaultFileBuffer;
import uk.q3c.build.gitplus.creator.gradle.ElementFactory;
import uk.q3c.build.gitplus.creator.gradle.FileBuffer;

/**
 * A possibly unnecessary kludge to get a DefaultFileBuffer object from Kotlin to Groovy (Spock) using this Java class as an intermediary
 * <p>
 * Created by David Sowerby on 02 Sep 2016
 */
@SuppressWarnings({"PublicMethodWithoutLogging", "ClassWithoutLogger", "UtilityClass", "MethodReturnOfConcreteClass"})
public class KotlinObjectFactory {

    public static FileBuffer fileBuffer() {
        return DefaultFileBuffer.INSTANCE;
    }

    public static ElementFactory elementFactory() {
        return ElementFactory.INSTANCE;
    }

}
