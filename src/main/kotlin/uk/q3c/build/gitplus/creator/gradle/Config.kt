package uk.q3c.build.gitplus.creator.gradle

/**
 * Created by David Sowerby on 30 Sep 2016
 */
class Config(name: String) : VariableNamedBlock(name) {

    init {
        writeWhenEmpty = true
    }

    fun config(name: String, init: Config.() -> Unit): Config {
        val cfg: Config = Config(name)
        cfg.init()
        elements.add(cfg)
        return cfg
    }

}