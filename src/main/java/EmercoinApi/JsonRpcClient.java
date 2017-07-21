package EmercoinApi;

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

import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

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

    public JSONObject callMethod(String method, Object[] params) throws Exception {
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

        if(jsonObject.get("error") != null) {
            throw new Exception(jsonObject.get("error").toString());
        }

        return jsonObject;
    }

    public File getFileFromNVS(String key) throws Exception {
        JSONObject response = this.callMethod("name_show", new Object[]{key});
        if(response.get("value") != null) {
            throw new Exception();
        }
        String base64Encoded = (String) ((JSONObject) response.get("result")).get("value");

        System.out.println(Base64.getDecoder().decode(base64Encoded));
        return new File(""); //TODO
    }

    public void putFileToNVS(String name, String filename, Integer days) throws Exception {
        Path path = Paths.get(filename);
        String content = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
        JSONObject response = this.callMethod("name_new", new Object[]{name, content, days});
        System.out.println(response.toJSONString());
    }





    /**
     * Function return all found json objects with similar name
     * as regex.
     * @param regexOfName - regex of destination name.
     * @return - all found json objects.
     */
    public List<JSONObject> findAllSimilarNames(String regexOfName) throws Exception
    {

        String emercoinCmd = "name_filter";
        JSONObject response = this.callMethod(emercoinCmd, new Object[]{regexOfName});

        List<JSONObject> jsonObjects = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) response.get("result");

        jsonArray.forEach(jsonObj->{
            jsonObjects.add((JSONObject) jsonObj);
        });

        return jsonObjects;
    }
}
