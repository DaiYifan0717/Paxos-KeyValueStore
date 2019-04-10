/**
 * This Server is a remote project.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface Server extends Remote {
    String request(Client client, String msg) throws RemoteException;
    String updateKV(String msg) throws RemoteException;
    HashMap<String, String> copy() throws RemoteException;
    Coordinator.Acceptor getAcceptor() throws RemoteException;
}
