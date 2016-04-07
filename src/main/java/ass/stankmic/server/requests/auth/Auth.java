package ass.stankmic.server.requests.auth;

import ass.stankmic.server.requests.Request;
import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;
import ass.stankmic.server.requests.exceptions.RequestToAccessFileException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

/**
 * Class with static methods to handle Basic access authentication.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class Auth {

    private Auth() {
    }

    /**
     * Name of the file where can be stored access credentials for its parent
     * directory and its subdirectories.
     */
    public static final String ACCESS_FILE_NAME = ".access_stankmic";

    /**
     * Check the request is authorized to get the response.
     *
     * @param request
     * @param baseDir base directory the server uses
     * @return true if the request is authorized
     * @throws BadHTTPRequestException when the Request is not formed well
     * @throws RequestToAccessFileException indicated request for file
     * containing Basic authentification credentials - forbidden
     */
    public static boolean isAuthorized(final Request request, final File baseDir) throws BadHTTPRequestException, RequestToAccessFileException {
        final File requsted = new File(baseDir, request.getPath());
        return isAuthorized(requsted, baseDir, request.getBasicAuthorization());
    }

    /**
     * Check the file is accessible with povided base64 encoded authorization.
     *
     * @param file
     * @param baseDir base directory the server uses
     * @param authorization base64 encoded
     * @return true if is accessible
     * @throws RequestToAccessFileException indicated request for file
     * containing Basic authentification credentials - forbidden
     */
    private static boolean isAuthorized(final File file, final File baseDir, final String authorization) throws RequestToAccessFileException {
        if (file.getName().equals(ACCESS_FILE_NAME)) {
            throw new RequestToAccessFileException();
        }
        final List<File> accessFiles = isProtected(file, baseDir);
        if (authorization == null || "".equals(authorization)) {
            return accessFiles.isEmpty();
        }
        return authorize(authorization, accessFiles);
    }

    /**
     * Check the file is protected with Basic access authentication.
     *
     * @param file
     * @param baseDir base directory the server uses
     * @return list of access files, empty if the file is not protected
     * @throws RequestToAccessFileException indicated request for file
     * containing Basic authentification credentials - forbidden
     */
    private static List<File> isProtected(final File file, final File baseDir) throws RequestToAccessFileException {
        if (file.getName().equals(ACCESS_FILE_NAME)) {
            throw new RequestToAccessFileException();
        }
        final List<File> accessFiles = new LinkedList<File>();
        File directory = file;
        File[] filesInDirectory;
        do {
            directory = directory.getParentFile();
            filesInDirectory = directory.listFiles();
            for (File f : filesInDirectory) {
                if (ACCESS_FILE_NAME.equals(f.getName())) {
                    accessFiles.add(f);
                }
            }
        } while (!directory.equals(baseDir));
        return accessFiles;
    }

    /**
     * Check the authorization matches the access files.
     *
     * @param authorization
     * @param accessFiles
     * @return true if the authorization matches
     */
    private static boolean authorize(final String authorization, final List<File> accessFiles) {
        final String credentials = new String(DatatypeConverter.parseBase64Binary(authorization));
        for (File accessFile : accessFiles) {
            if (containsCredentials(accessFile, credentials)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the access file contains given credentials.
     *
     * @param accessFile
     * @param credentials
     * @return true if the access file contains given credentials
     */
    private static boolean containsCredentials(final File accessFile, final String credentials) {
        try {
            final InputStream is = new FileInputStream(accessFile);
            final BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals(credentials)) {
                    br.close();
                    is.close();
                    return true;
                }
            }
            br.close();
            is.close();
        } catch (IOException ex) {
            return false;
        }
        return false;
    }

}
