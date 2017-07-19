import EmercoinApi.JsonRpcClient;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by a.barabanov on 19.07.2017.
 */
public class Main {
    public static void main(String[] args) throws Exception {

        JsonRpcClient rpcClient = new JsonRpcClient("127.0.0.1", 6662, "emccoinrpc", "secret");

        rpcClient.getFileFromNVS("inno:test");
//        rpcClient.putFileToNVS("inno:test", "test.txt", 1);

    }
}
