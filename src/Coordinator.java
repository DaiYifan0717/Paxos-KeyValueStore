import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Coordinator implements Paxos {

    private static int voteNumber = 0;

    private static CoordinatorLogger logger;

    /*
     * All the port number of servers in the distributed system.
     */
    private static ArrayList<Integer> serverPort;

    private static int coordinatorPort = 8099;

    public static void main(String[] args) {

        serverPort = new ArrayList<Integer>();

        if(args.length > 0) {
            coordinatorPort = Integer.parseInt(args[0]);
        }

        for(int i = 1; i < args.length; i++) {
            serverPort.add(new Integer(args[i]));
        }

        logger = new CoordinatorLogger();

        Paxos stub;
        Registry registry;

        try {
            Paxos obj = new Coordinator();
            stub = (Paxos) UnicastRemoteObject.exportObject(obj, 0);

            LocateRegistry.createRegistry(coordinatorPort);
            // Bind the remote object's stub in the registry
            registry = LocateRegistry.getRegistry(coordinatorPort);
            logger.commonInfoLog("This Coordinator is starting in port: " + Integer.toString(coordinatorPort) + ".");

            registry.bind("Coordinator" , stub);
            logger.commonInfoLog("Coordinator is ready now!");
        } catch (Exception e) {
            logger.commonErrorLog("Coordinator exception: " + e.getMessage());
            System.exit(1);
        }
    }

    public boolean execute(String msg) {
        List<Acceptor> acceptors = new ArrayList<Acceptor>();

        Server stub;
        Registry registry;

        for (int i = 0; i < serverPort.size(); i++) {
            try {

                registry = LocateRegistry.getRegistry(serverPort.get(i));

                stub = (Server) registry.lookup("KeyValueStoreGeneralServer" +
                        Integer.toString(serverPort.get(i)));

                acceptors.add(stub.getAcceptor());
            } catch (Exception e) {
                logger.commonErrorLog("Server " + Integer.toString(serverPort.get(i)) + " is down.");
            }
        }

        boolean result = Proposer.vote(new Proposal(++voteNumber, msg), acceptors);

        return result;
    }

    /**
     * All the KV replicas learn to update.
     */
    public void learn(String msg, int portNumber) {

        Server stub;
        Registry registry;

        for (int i = 0; i < serverPort.size(); i++) {
            try{
                if(serverPort.get(i) == portNumber) continue;
                registry = LocateRegistry.getRegistry(serverPort.get(i));

                stub = (Server) registry.lookup("KeyValueStoreGeneralServer" +
                        Integer.toString(serverPort.get(i)));

                stub.updateKV(msg);

            } catch (Exception e) {
                logger.commonErrorLog("Server " + Integer.toString(serverPort.get(i)) + " is down.");
            }
        }
    }


    protected static class Promise implements Serializable {

        protected final boolean ack;
        protected final Proposal proposal;

        public Promise(boolean ack, Proposal proposal) {
            this.ack = ack;
            this.proposal = proposal;
        }

        public boolean isAck() {
            return ack;
        }

        public Proposal getProposal() {
            return proposal;
        }
    }

    protected static class Proposal implements Comparable<Proposal>, Serializable {

        protected final long voteNumber;
        protected final String content;

        public Proposal(long voteNumber, String content) {
            this.voteNumber = voteNumber;
            this.content = content;
        }

        public Proposal() {
            this(0, null);
        }

        public long getVoteNumber() {
            return voteNumber;
        }

        @Override
        public int compareTo(Proposal o) {
            return Long.compare(voteNumber, o.voteNumber);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof Proposal))
                return false;
            Proposal proposal = (Proposal) obj;
            return voteNumber == proposal.voteNumber
                    && (content != null && content.equals(proposal.content));
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append(voteNumber)
                    .append(": \"")
                    .append(content)
                    .append("\"")
                    .toString();
        }
    }

    protected static void printInfo(String subject, String operation, String result) {
        System.out.println(subject + " :" + operation + "<" + result + ">");
    }

    public static class Proposer implements Serializable {

        /**
         * @param proposal
         * @param acceptors
         */
        public static boolean vote(Coordinator.Proposal proposal, Collection<Acceptor> acceptors) {

            int quorum = Math.floorDiv(acceptors.size(), 2) + 1;
            List<Coordinator.Proposal> proposals = new ArrayList<Proposal>();
            for (Acceptor acceptor : acceptors) {
                Coordinator.Promise promise = acceptor.onPrepare(proposal);
                if (promise != null && promise.isAck())
                    proposals.add(promise.getProposal());
            }
            if (proposals.size() < quorum) {
                printInfo("PROPOSER[" + proposal + "]", "VOTE", "NOT PREPARED");
                return false;
            }
            int acceptCount = 0;
            for (Acceptor acceptor : acceptors) {
                if (acceptor.onAccept(proposal))
                    acceptCount++;
            }
            if (acceptCount < quorum) {
                printInfo("PROPOSER[" + proposal + "]", "VOTE", "NOT ACCEPTED");
                return false;
            }

            printInfo("PROPOSER[" + proposal + "]", "VOTE", "SUCCESS");
            return true;
        }
    }

    public static class Acceptor implements Serializable {

        // last vote result
        public Proposal last = new Proposal();
        public String name;

        public Acceptor(String name) {
            this.name = name;
        }

        public Promise onPrepare(Proposal proposal) {
            // We suppose that the failure probability is 10%
            if (Math.random() < 0.1) {
                printInfo("Server " + name, "PREPARE", "NO RESPONSE");
                return null;
            }
            if (proposal == null)
                throw new IllegalArgumentException("null proposal");
            if (proposal.getVoteNumber() > last.getVoteNumber()) {
                Promise response = new Promise(true, last);
                last = proposal;
                printInfo("Server " + name, "PREPARE", "OK");
                return response;
            } else {
                printInfo("Server " + name, "PREPARE", "REJECTED");
                return new Promise(false, null);
            }
        }

        public boolean onAccept(Proposal proposal) {
            // We suppose that the failure probability is 10%
            if (Math.random() < 0.1) {
                printInfo("Server " + name, "ACCEPT", "NO RESPONSE");
                return false;
            }
            printInfo("Server " + name, "ACCEPT", "OK");

            return last.equals(proposal);
        }
    }
}
