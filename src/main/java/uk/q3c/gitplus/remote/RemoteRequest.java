package uk.q3c.gitplus.remote;

import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;

import javax.json.JsonReader;
import java.io.IOException;

/**
 * Created by David Sowerby on 21 Mar 2016
 */
public class RemoteRequest {

    public JsonReader request(String method, String uri, String apiToken) throws IOException {
        Request request = new JdkRequest(uri).header("Accept", "application/vnd.github.v3+json")
                                             .header("Authorization", "token " + apiToken);
        Response response = request.method(method)
                                   .fetch();
        return response.as(JsonResponse.class)
                       .json();
    }
}
