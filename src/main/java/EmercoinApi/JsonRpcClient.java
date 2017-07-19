package EmercoinApi;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

/**
 * Created by a.barabanov on 19.07.2017.
 */
public class JsonRpcClient {

    private URI uri;

    public JsonRpcClient(String rpcurl, Integer rpcport, String rpcuser, String rpcpassword) {
        try {
            uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(rpcurl)
                    .setPort(rpcport)
                    .setUserInfo(rpcuser, rpcpassword)
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public JSONObject callMethod(String method, String[] params) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();

        HttpPost request = new HttpPost(uri);
        request.setHeader("Accept", "application/json");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", method);
        jsonObject.put("params", Arrays.asList(params));

        StringEntity requestBody = new StringEntity(jsonObject.toJSONString());
        requestBody.setContentType("application/json");
        request.setEntity(requestBody);

        HttpResponse response = client.execute(request);

        System.out.println("Calling method : " + method);
        System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

        JSONParser parser = new JSONParser();
        try {
            jsonObject = (JSONObject) parser.parse(new InputStreamReader(response.getEntity().getContent()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public File getFileFromNVS(String key) throws Exception {
        JSONObject response = null;
        try {
            response = this.callMethod("name_show", new String[]{key});
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(response.get("error") != null) {
            throw new Exception("Name value pair not found");
        }

        System.out.println(response);

        return new File("");
    }

    public void putFileToNVS(String name, String filename, String days) throws IOException {
        Path path = Paths.get(filename);
        String content = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
        JSONObject response = this.callMethod("name_new", new String[]{name, content, days});
    }
}
