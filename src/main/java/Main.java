import EmercoinApi.JsonRpcClient;

/**
 * Created by a.barabanov on 19.07.2017.
 */
public class Main {

    public static void main(String[] args) throws Throwable {

        JsonRpcClient client = new JsonRpcClient("emccoinrpc", "secret");

        client.getVerifiedDpoItems("iteco");
    }
}
