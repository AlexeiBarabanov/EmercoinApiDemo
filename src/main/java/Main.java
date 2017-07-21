import EmercoinApi.JsonRpcClient;

/**
 * Created by a.barabanov on 19.07.2017.
 */
public class Main {
    public static void main(String[] args) throws Exception {

        JsonRpcClient rpcClient = new JsonRpcClient("127.0.0.1", 6662, "emccoinrpc", "secret");

//        rpcClient.getFileFromNVS("inno:test");
//        rpcClient.putFileToNVS("inno:test", "test.txt", 1);
        rpcClient.getVerifiedDpoItems("iteco");
//        System.out.println(rpcClient.callMethod("signmessage", new Object[]{"EJ5Lvzg6bhkVm5iRQV4kLd85daBPP6rtVH", "message"}));

    }
}
