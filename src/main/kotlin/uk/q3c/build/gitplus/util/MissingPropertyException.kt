package uk.q3c.build.gitplus.util

/**
 * Created by David Sowerby on 04 Apr 2016
 */
class MissingPropertyException(key: String) : RuntimeException("The $key property is missing or has no value")
