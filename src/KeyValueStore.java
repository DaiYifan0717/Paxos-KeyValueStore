import java.util.HashMap;

@SuppressWarnings("unchecked")

public class KeyValueStore {
    private HashMap<String, String> store;
    ServerLogger logger;

    public KeyValueStore(ServerLogger _logger) {
        store = new HashMap<String, String>();
        logger = _logger;
    }

    public HashMap<String, String> getKV() {
        return store;
    }

    public void setKV(HashMap<String, String> KVcopy) {
        store = (HashMap<String, String>)(KVcopy.clone());
    }

    /**
     * @param input a string which stand for command from the client
     * @return the result of the command from the client after execution.
     */
    public synchronized String execute(String input) {
        // Get individual items in the input string.
        String[] items = input.split("[^a-zA-Z0-9/]+");

        System.out.print(String.format("[%1$tF %1$tl:%1$tM:%1$tS.%1$tL %1$Tp]", System.currentTimeMillis()) + " ");

        // put
        if (items[0].toLowerCase().equals("put")){
            if(items.length != 3){
                System.out.println("Response: \"Please input in the form of: put key value\"");
                logger.invalidPutLog();
                return("Please input in the form of: put key value");
            }

            return put(items[1], items[2]);
        }

        // get
        if (items[0].toLowerCase().equals("get")){
            if(items.length != 2){
                System.out.println("Response: \"Please input in the form of: get key\"");
                logger.invalidGetLog();
                return("Please input in the form of: get key");
            }
            return get(items[1]);
        }

        // delete
        if (items[0].toLowerCase().equals("delete")){
            if(items.length != 2){
                System.out.println("Response: \"Please input in the form of: delete key\"");
                logger.invalidDeleteLog();
                return("Please input in the form of: delete key");
            }
            return delete(items[1]);
        }

        // get size
        if (items[0].toLowerCase().equals("size")){
            if(items.length != 1){
                System.out.println("Response: \"No arguments accepted after size\"");
                logger.invalidSizeLog();
                return("No arguments accepted after size");
            }
            return size();
        }

        System.out.println("Response: \"There are only three operation accepted: 1. put 2. get 3. delete\"");
        logger.invalidCommandLog();
        return ("There are only three operation accepted: 1. put 2. get 3. delete");
    }

    /**
     * @param key key of the map elements
     * @param value value of the map elements
     * @return A string stand for successfully insert or a string stand for successfully update.
     */
    private String put(String key, String value) {
        if(store.get(key) == null) {
            store.put(key, value);
            System.out.println("Response: \"" + "Set the key value pairs: " + key + " -> " + value + "\"");
            logger.successPutUpdateLog();
            return ("Set the key value pairs: " + key + " -> " + value);
        }
        String previousValue = store.get(key);
        store.put(key, value);
        System.out.println("Response: \"" + "Reset the key value pairs from: " + key + " -> " + previousValue +
                " to: " + key + " -> " + value + "\"");
        logger.successPutSetLog();
        return ("Reset the key value pairs from: " + key + " -> " + previousValue +
                " to: " + key + " -> " + value);
    }

    /**
     * @param key key of the map elements
     * @return A string stand for successfully get or a string stand for failure (non-exist).
     */
    private String get(String key) {
        String value = store.get(key);
        if(value == null){
            System.out.println("Response: \"" + "The key: " + key + " is not exist!" + "\"");
            logger.nonexistKeyGetLog();
            return("The key: " + key + " is not exist!");
        }
        System.out.println("Response: \"" + "The value of the key: " + key + " is " + value + "\"");
        logger.successGetLog();
        return("The value of the key: " + key + " is " + value);
    }

    /**
     * @param key key of the map elements
     * @return A string stand for successfully delete or a string stand for failure (non-exist).
     */
    private String delete(String key) {
        if(store.get(key) == null){
            System.out.println("Response: \"" + "The key: " + key + " is not exist!" + "\"");
            logger.nonexistKeyDeleteLog();
            return("The key: " + key + " is not exist!");
        }
        store.remove(key);
        System.out.println("Response: \"" + "Successfully delete the key value pairs of the key: " + key + "\"");
        logger.successDeleteLog();
        return("Successfully delete the key value pairs of the key: " + key);
    }

    /**
     * @return the size of the key-value-store
     */
    public String size() {
        System.out.println("Response: \"" + "The current size of the key value store is " + store.size() + "\"");
        logger.successSizeLog();
        return("The current size of the key value store is " + store.size());
    }
}
