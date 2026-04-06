package process;

import iop.Iop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author ByXanum
 */
public class C_ClientHTTP {  // === HTTP POST con HttpClient ===
    
    private final String URL; 

    public C_ClientHTTP(String _url) {
        this.URL = _url;
    }
    
    public JSONObject post(String _pathUrl, JSONObject _body) throws Exception {
        HttpClient y_client = HttpClient.newHttpClient();
        HttpRequest y_request = HttpRequest.newBuilder()
                .uri(URI.create(this.URL + _pathUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(_body.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = y_client.send(y_request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new RuntimeException("HTTP " + response.statusCode());
        return new JSONObject(response.body());
    }
    

}

    


    
    
