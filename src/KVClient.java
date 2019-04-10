import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class KVClient implements Client{

    /*
     * All the port number of servers in the distributed system.
     */
    private static ArrayList<Integer> serverPort;

    private static Client client;

    public static void main(String[] args) {

        client = new KVClient();
        ClientLogger logger = new ClientLogger();

        serverPort = new ArrayList<Integer>();

        for(int i = 0; i < args.length; i++) {
            serverPort.add(Integer.parseInt(args[i]));
        }

        try {
            UnicastRemoteObject.exportObject(client, 0);
        } catch (Exception e) {
            logger.commonErrorLog("KVClient exception: " + e.toString());
            System.exit(1);
        }

        logger.commonInfoLog("KVClient Start!");

        Random RANDOM = new Random();

        BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in));

        String keyboardInput;

        try {
            while (!(keyboardInput = keyboardReader.readLine()).equals("quit")){

                /*
                 * Wait until finding a available server.
                 */
                while(true) {
                    try {
                        // Can send request to server unless the client quit.
                        int portNumber = serverPort.get(RANDOM.nextInt(serverPort.size()));
                        Registry registry = LocateRegistry.getRegistry(portNumber);
                        Server stub = (Server) registry.lookup("KeyValueStoreGeneralServer" + Integer.toString(portNumber));

                        String response = stub.request(client, keyboardInput);

                        System.out.println(response);
                        break;
                    } catch (Exception e) {
                        continue;
                    }
                }
            } 
        } catch (Exception e){
        
        }
    }
}
