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
import java.io.IOException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by a.barabanov on 19.07.2017.
 */
public class JsonRpcClient {

    private static final String service = "dpo";
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

    public JSONObject callMethod(String method, Object...params) throws Exception {
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
        JSONObject response = this.callMethod("name_show", key);
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
        JSONObject response = this.callMethod("name_new", name, content, days);
        System.out.println(response.toJSONString());
    }


    /**
     * Function return all found json objects with similar name
     * as regex.
     * @param regexOfName - regex of destination name.
     * @return - all found json objects.
     */
    private List<JSONObject> findAllSimilarNames(String regexOfName) throws Exception
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

    public JSONObject getValueFromNVS(String name) throws Exception {
        return this.callMethod("name_show", name);
    }

    public boolean verifyMessage(String address, String signature, String message) {
        String result = null;
        try {
            result = this.callMethod("verifymessage", address, signature, message).get("result").toString();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Boolean(result);
    }

    public boolean proveOwnership(JSONObject serialItem) {
        Pattern pattern = Pattern.compile("^Signature=(.*)$");
        String serialName = (String) serialItem.get("name");
        String value = (String) serialItem.get("value");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {

            String address = null;
            try {
                JSONObject result = (JSONObject) this.getValueFromNVS(serialName).get("result");
                address = (String) result.get("address");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String signature = matcher.group(1);
            boolean result = verifyMessage(address, signature, serialName);
            return result;
        } else {
            System.out.println("Signature not found for dpo item: " + serialName);
        }
        return false;
    }

    public void getVerifiedDpoItems(String name) throws Exception {
        String filter = "^" + service + ":" + name + ":.+";

        JSONObject response = this.callMethod("name_filter", filter);
        if(response.get("error") != null)
            throw new Exception("Error occurred during execution");

        JSONArray serialItems = (JSONArray) response.get("result");
        if(serialItems.isEmpty())
            throw new Exception("Specified brand was not found in NVS");

        for(int i = 0; i < serialItems.size(); i++) {
            System.out.println();
            proveOwnership((JSONObject) serialItems.get(i));
        }

    public boolean putDocumentToDPO(String docname, String emercoinaddress, String localFilename) {
        Path path = Paths.get(localFilename);
        String vendor = "iteco";
        try {
            String stringBase64 = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
            String signature = signMessage(emercoinaddress, stringBase64);
            if (signature != null ) {
                String value = "Signature=" + signature;
                String serialNumber = "dpo:" + vendor + ":" + docname + ":0";
                callMethod("name_new", serialNumber, value, 10, emercoinaddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private String signMessage(String emercoinaddress, String message) {
        String signature = null;
        try {
            JSONObject signedMessage = callMethod("signmessage", emercoinaddress, message);
            if (signedMessage.containsKey("result")) {
                signature = (String)signedMessage.get("result");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signature;
    }
}
