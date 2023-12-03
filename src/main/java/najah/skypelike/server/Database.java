package najah.skypelike.server;

import najah.skypelike.client.ClientApplication;
import najah.skypelike.common.Functions;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *      database class is the class responsible to deal with users file
 *      check credentials correctness and add new users.
 */
public class Database {

    /**
     * to check if given name and password pair exist in the database or not
     * return true if exist and false otherwise.
     *
     * @param name - name of user
     * @param password - password of user
     * @return - true if user exist and false otherwise
     * @throws IOException - reading file checked exception
     * @throws NoSuchAlgorithmException - encryption to sha256 exception
     */
    public static boolean checkCredentials(String name, String password) throws IOException, NoSuchAlgorithmException {
        var url = Database.class.getResource("users.txt");
        assert url != null;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(url.getFile()));
        String line;
        String credentials = name + " " + Functions.encrypt(password.toLowerCase());
        while ((line = bufferedReader.readLine()) != null) {
            if (line.equalsIgnoreCase(credentials)) return true;
        }
        return false;
    }
}
