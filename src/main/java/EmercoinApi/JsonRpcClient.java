package EmercoinApi;

import com.googlecode.jsonrpc4j.IJsonRpcClient;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import java.io.File;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by a.barabanov on 19.07.2017.
 */
public class JsonRpcClient {

    private static final String SERVICE = "dpo";
    public static String LOCALHOST = "http://127.0.0.1";
    public static String PORT = "6662";
    public static IJsonRpcClient client;

    public JsonRpcClient(String username, String password, String rpcurl, String rpcport) throws Exception {

        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        });

        PORT = rpcport;
        client = new JsonRpcHttpClient(new URL(rpcurl + ":" + rpcport));
    }

    public JsonRpcClient(String username, String password) throws Exception {
        this(username, password, LOCALHOST, PORT);
    }

    public File getFileFromNVS(String name) throws Throwable {
        String base64Encoded = getValueFromNVS(name);

        System.out.println(Base64.getDecoder().decode(base64Encoded));
        return new File(""); //TODO
    }

    public String getValueFromNVS(String name) throws Throwable {
        return nameShow(name).get("value");
    }

    public Map<String, String> nameShow(String name) throws Throwable {
        return client.invoke("name_show", new Object[]{name}, LinkedHashMap.class);
    }

    public Boolean verifyMessage(String address, String signature, String message) throws Throwable {
        return client.invoke("verifymessage", new Object[]{address, signature, message}, Boolean.class);
    }

    public Boolean proveOwnership(String rootAddress, Map<String, String> dpoEntry) throws Throwable {
        Pattern pattern = Pattern.compile("(?)Signature=(.*)");
        Matcher matcher = pattern.matcher(dpoEntry.get("value"));
        if (matcher.find()) {
            String signature = matcher.group(1);
            return verifyMessage(rootAddress, signature, dpoEntry.get("name"));
        } else {
            System.out.println("Signature not found for dpo item: " + dpoEntry.get("name"));
        }
        return false;
    }

    public void getVerifiedDpoItems(String brandName) throws Throwable {
        String filter = "^" + SERVICE + ":" + brandName + ":.+";
        String rootDpoAddress = nameShow(SERVICE + ":" + brandName).get("address");

        List<LinkedHashMap<String, String>> result = client.invoke("name_filter", new Object[]{filter, 0}, LinkedList.class);
        for (LinkedHashMap<String, String> dpoEntry : result) {
            System.out.println("Brand: " + brandName);
            System.out.println("Serial: " + dpoEntry.get("name"));
            System.out.println("Verified: " + proveOwnership(rootDpoAddress, dpoEntry));
            System.out.println();
        }
    }

    public boolean putDocumentToDPO(String docname, String emercoinaddress, String localFilename) throws Throwable {
        Path path = Paths.get(localFilename);
        String vendor = "iteco";
        try {
            String stringBase64 = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
            String signature = signMessage(emercoinaddress, stringBase64);
            if (signature != null) {
                String value = "doc=" + stringBase64 + " Signature=" + signature;
                String serialNumber = "dpo:" + vendor + ":" + docname + ":0";
//                callMethod("name_new", serialNumber, value, 10, emercoinaddress);
                client.invoke("name_new", new Object[]{serialNumber, value, 10, emercoinaddress});
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private String signMessage(String emercoinaddress, String message) throws Throwable {
        return client.invoke("signmessage", new Object[]{emercoinaddress, message}, String.class);
    }
}
