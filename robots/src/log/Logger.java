package log;

public final class Logger {
    private static final LogWindowSource defaultLogSource;

    static { defaultLogSource = new LogWindowSource(100); }
    
    private Logger() {}

    public static void debug(String strMessage) {
        defaultLogSource.append(LogLevel.Debug, strMessage);
    }

    public static LogWindowSource getDefaultLogSource() {
        return defaultLogSource;
    }

    public static void clear() {
        getDefaultLogSource().clear();
    }
}
