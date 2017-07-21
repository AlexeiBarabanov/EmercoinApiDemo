package EmercoinApi;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class EmcClient {
    private JsonRpcClient jsonRpcClient = new JsonRpcClient("127.0.0.1", 6662, "emccoinrpc",
            "secret_password");

    public boolean putDocumentToDPO(String emercoinaddress, String localFilename) {
        Path path = Paths.get(localFilename);
        try {
            String stringBase64 = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
            System.out.println(signMessage(emercoinaddress, stringBase64));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public String signMessage(String emercoinaddress, String message) {
        String signature = null;
        try {
            JSONObject signedMessage = jsonRpcClient.callMethod("signmessage", emercoinaddress, message);
            if (signedMessage.containsKey("result")) {
                signature = (String)signedMessage.get("result");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signature;
    }
}

