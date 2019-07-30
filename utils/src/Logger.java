import java.util.Arrays;
import java.util.List;

public class Logger {

    public static final String MAND_FLAG = "MAND_FLAG";
    public static final String ERROR = "ERROR";
    private List<String> flags;

    public Logger(final String... flags) {
        this.flags = Arrays.asList(flags);
        this.flags.add(MAND_FLAG);
        this.flags.add(ERROR);
    }

    public void addFlag(final String flag) {
        this.flags.add(flag);
    }

    public void removeFlag(final String flag) {
        this.flags.remove(flag);
    }

    public List<String> getFlags() {
        return this.flags;
    }

    public void log(final String message) {
        log(MAND_FLAG, message);
    }

    public void log(final String flag, final String message) {
        if (this.flags.contains(flag)) {
            System.out.println(message);
        }
    }

    public void logError(final String error) {
        logError(ERROR, error);
    }

    private void logError(String flag, String error) {
        if (this.flags.contains(flag)) {
            System.err.println(error);
        }
    }
}
