import java.util.logging.Level;

class ServerLogger {
    private static java.util.logging.Logger LOGGER;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tl:%1$tM:%1$tS.%1$tL %1$Tp] [%4$-7s] %5$s %n");
        LOGGER = java.util.logging.Logger.getLogger(ServerLogger.class.getName());
    }

    public void commonInfoLog(String s) {LOGGER.info(s);}

    public void commonErrorLog(String s) {LOGGER.warning(s);}

    public void clientJoinLog(int hashCode) {LOGGER.info("Client with hash code: " + hashCode + " is connected!");}

    public void invalidCommandLog() {
        LOGGER.warning("Receive invalid request to key value store.");
    }

    public void invalidPutLog() {
        LOGGER.warning("Receive invalid request PUT with wrong input format.");
    }

    public void invalidGetLog() {
        LOGGER.warning("Receive invalid request GET with wrong input format.");
    }

    public void invalidDeleteLog() {
        LOGGER.warning("Receive invalid request DELETE with wrong input format.");
    }

    public void invalidSizeLog() {
        LOGGER.warning("Receive invalid request SIZE with wrong input format.");
    }

    public void successPutSetLog() {
        LOGGER.log(Level.INFO, "Successful request PUT with setting a new key-value pair.");
    }

    public void successPutUpdateLog() {
        LOGGER.log(Level.INFO, "Successful request PUT with updating an key-value pair.");
    }

    public void successGetLog() {
        LOGGER.log(Level.INFO, "Successful request GET.");
    }

    public void nonexistKeyGetLog() {
        LOGGER.warning("The key for request GET is non-existed");
    }

    public void successDeleteLog() {
        LOGGER.log(Level.INFO, "Successful request DELETE.");
    }

    public void nonexistKeyDeleteLog() {
        LOGGER.warning("The key for request DELETE is non-existed");
    }

    public void successSizeLog() {
        LOGGER.log(Level.INFO, "Successful request SIZE.");
    }
}
