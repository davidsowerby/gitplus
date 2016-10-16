package uk.q3c.build.gitplus.remote

import com.jcabi.http.request.JdkRequest
import com.jcabi.http.response.JsonResponse
import javax.json.JsonReader

/**
 * Created by David Sowerby on 21 Mar 2016
 */
class DefaultRemoteRequest : RemoteRequest {

    override fun request(method: String, uri: String, apiToken: String): JsonReader {
        val request = JdkRequest(uri).header("Accept", "application/vnd.github.v3+json").header("Authorization", "token " + apiToken)
        val response = request.method(method).fetch()
        return response.`as`(JsonResponse::class.java).json()
    }
}
