package uk.q3c.build.gitplus.remote

import javax.json.JsonReader

/**
 * Created by David Sowerby on 23 Oct 2016
 */
interface RemoteRequest {
    fun request(method: String, uri: String, apiToken: String): JsonReader
}