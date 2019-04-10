/**

 * Paxos

 * Phase 1

 * (a) A proposer selects a proposal number n and sends a prepare request with number n to a majority of acceptors.

 * (b) If an acceptor receives a prepare request with number n greater than that of any prepare request to which it has already responded, then it responds to the request with a promise not to accept any more proposals numbered less than n and with the highest-numbered proposal (if any) that it has accepted.

 * Phase 2

 * (a) If the proposer receives a response to its prepare requests (numbered n) from a majority of acceptors, then it sends an accept request to each of those acceptors for a proposal numbered n with a value v , where v is the value of the highest-numbered proposal among the responses, or is any value if the responses reported no proposals.

 * (b) If an acceptor receives an accept request for a proposal numbered n, it accepts the proposal unless it has already responded to a prepare request having a number greater than n.

 * Learn

 * If phase 1 and phase 2 all pass, then each replicas learn the latest update.

 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Paxos extends Remote {
    boolean execute(String msg) throws RemoteException;
    void learn(String msg, int portNumber) throws RemoteException;
}
