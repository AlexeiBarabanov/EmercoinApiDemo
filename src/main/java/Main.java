import EmercoinApi.JsonRpcClient;

import java.io.IOException;

/**
 * Created by a.barabanov on 19.07.2017.
 */
public class Main {
    public static void main(String[] args) throws Exception {

        JsonRpcClient rpcClient = new JsonRpcClient("127.0.0.1", 6662, "emccoinrpc", "secret");

        rpcClient.getFileFromNVS("testNameKekLol");
//        rpcClient.putFileToNVS("test.txt", "inno:test");

    }
}
