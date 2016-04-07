package ass.stankmic.main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Switch enum is used to identify arguments easily.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public enum Switch {

    HELP,
    PORT,
    BASE_DIR,
    UNKNOWN;
    private static final Pattern helpRegex = Pattern.compile("-*h(elp){0,1}", Pattern.CASE_INSENSITIVE),
            portRegex = Pattern.compile("-*p(ort){0,1}", Pattern.CASE_INSENSITIVE),
            dirRegex = Pattern.compile("-*d(ir){0,1}", Pattern.CASE_INSENSITIVE);

    private Switch() {
    }

    /**
     * Returns enum value according to the given argument string.
     *
     * @param arg argument to identify
     * @return Switch enum according to the given arg
     */
    protected static Switch recognizeSwitch(final String arg) {
        final Matcher help = helpRegex.matcher(arg);
        if (help.matches()) {
            return HELP;
        }

        final Matcher port = portRegex.matcher(arg);
        if (port.matches()) {
            return PORT;
        }

        final Matcher dir = dirRegex.matcher(arg);
        if (dir.matches()) {
            return BASE_DIR;
        }

        return UNKNOWN;
    }
}
