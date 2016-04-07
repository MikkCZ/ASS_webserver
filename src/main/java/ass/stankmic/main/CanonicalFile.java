package ass.stankmic.main;

import java.io.File;
import java.io.IOException;

/**
 * Can return canonical/absolute version of the given File.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class CanonicalFile {

    private CanonicalFile() {
    }

    /**
     * Returns canonical or absolute "clone" of the given File.
     *
     * @param file to canonize
     * @return canonical "clone" of the given File
     */
    public static File get(final File file) {
        File toReturn;
        try {
            toReturn = file.getCanonicalFile();
        } catch (IOException ex) {
            toReturn = file.getAbsoluteFile();
        }
        return toReturn;
    }

}
