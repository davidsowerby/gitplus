package uk.q3c.build.gitplus.gitplus

import com.fasterxml.jackson.annotation.JsonIgnore
import uk.q3c.build.gitplus.util.FilePropertiesLoader
import uk.q3c.build.gitplus.util.PropertiesLoader

/**
 * Created by David Sowerby on 06 Sep 2017
 */
interface GitPlusConfiguration {
    /**
     * Version of configuration structure. Has to be var to allow loading from JSON
     */
    var version: Int
    /**
     * One or more loaders to retrieve build properties (mostly API tokens) from the environment this library is deployed in.
     * By default contains an instance of [FilePropertiesLoader].  Modify by calling [propertiesLoaders.add] etc.
     * The order of loaders is important, as it is the first loader to provide a value which is used
     */
    var propertiesLoaders: MutableList<PropertiesLoader>
}

class DefaultGitPlusConfiguration : GitPlusConfiguration {
    override var version = 1
    @JsonIgnore
    override var propertiesLoaders: MutableList<PropertiesLoader> = mutableListOf(FilePropertiesLoader().sourceFromGitPlus())
}