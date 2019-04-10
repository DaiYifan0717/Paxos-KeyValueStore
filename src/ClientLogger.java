public class ClientLogger {
    private static java.util.logging.Logger LOGGER;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tl:%1$tM:%1$tS.%1$tL %1$Tp] [%4$-7s] %5$s %n");
        LOGGER = java.util.logging.Logger.getLogger(ClientLogger.class.getName());
    }

    public void commonInfoLog(String s) {LOGGER.info(s);}

    public void commonErrorLog(String s) {LOGGER.warning(s);}
}
