import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This Server using Coordinator for the consensus.
 */

public class KVServer implements Server {

    private static ServerLogger logger;
    private static KeyValueStore store;

    /*
     * The port number of this server instance.
     */
    public static int portNumber = 1099;

    private static int coordinatorPort = 8099;

    private static Coordinator.Proposer proposer;
    private static Coordinator.Acceptor acceptor;

    private static List<Client> clients;

    public Coordinator.Acceptor getAcceptor() {
        return acceptor;
    }

    public HashMap<String, String> copy(){
        return store.getKV();
    }

    private static boolean recover(Integer restorePort) {

        Server stub;
        Registry registry;

        try {
            registry = LocateRegistry.getRegistry(restorePort);

            stub = (Server) registry.lookup("KeyValueStoreGeneralServer" +
                    Integer.toString(restorePort));

            store.setKV(stub.copy());

            logger.commonInfoLog("This Server recover from the live server " + Integer.toString(restorePort));

            return true;
        } catch (Exception e){
        }
        return false;
    }

    public static void main(String[] args) {

        if(args.length > 0) {
            portNumber = Integer.parseInt(args[0]);
        }

        logger = new ServerLogger();
        store = new KeyValueStore(logger);
        clients = new ArrayList<Client>();

        for(int i = 1; i < args.length; i++) {
            if(recover(Integer.parseInt(args[i]))){
                break;
            }
        }

        Server stub;
        Registry registry;

        try {
            KVServer obj = new KVServer();
            stub = (Server) UnicastRemoteObject.exportObject(obj, 0);

            System.out.println(portNumber);

            LocateRegistry.createRegistry(portNumber);
            // Bind the remote object's stub in the registry
            registry = LocateRegistry.getRegistry(portNumber);
            logger.commonInfoLog("This Key Value Server is starting in port: " + Integer.toString(portNumber) + ".");

            registry.bind("KeyValueStoreGeneralServer" + Integer.toString(portNumber), stub);

            logger.commonInfoLog("Server " + Integer.toString(portNumber) + " is ready now!");

            proposer = new Coordinator.Proposer();
            acceptor = new Coordinator.Acceptor(Integer.toString(portNumber));

        } catch (Exception e) {
            logger.commonErrorLog("Server exception: " + e.getMessage());
            System.exit(1);
        }
    }

    public String request(Client client, String msg) {

        if(!clients.contains(client)){
            logger.clientJoinLog(client.hashCode());
            clients.add(client);
        }

        String result = "";

        try {

            Registry registry = LocateRegistry.getRegistry(coordinatorPort);

            Paxos stub = (Paxos) registry.lookup("Coordinator");

            boolean ans = stub.execute(msg);

            if(ans){
                result = store.execute(msg);
                if(isPutOrDelete(msg)) {
                    stub.learn(msg, portNumber);
                }
            } else {
                return "This request is rejected!";
            }
        } catch (Exception e) {

        }
        return result;
    }

    private boolean isPutOrDelete(String msg) {
        String[] items = msg.split("[^a-zA-Z0-9/]+");
        if (items[0].toLowerCase().equals("put") || items[0].toLowerCase().equals("delete")){
            return true;
        }
        return false;
    }

    public String updateKV(String msg) {
        return store.execute(msg);
    }
}
