package najah.skypelike.common;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * class that have static methods and attributes which implements
 * various functionalities such as encryption and validation.
 */
public class Functions {

    /**
     * function to check validity for ip address format
     *
     * @param ip - ip to validate
     * @return - true if valid and false otherwise
     */
    public static boolean validateIP(String ip) {
        String IPRegex = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}" +
                "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
        Pattern p1 = Pattern.compile(IPRegex);
        return p1.matcher(ip).matches();
    }

    /**
     * method to check validity for port number
     *
     * @param port - port to validate
     * @return - true if valid and false otherwise
     */
    public static boolean validatePort(String port) {
        String portRegex = "^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|" +
                "65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";
        Pattern p2 = Pattern.compile(portRegex);
        return p2.matcher(port).matches();
    }

    /**
     * get the encryption stream of given string in sha256 algorithm
     *
     * @param input - string to encrypt
     * @return - byte[] stream of encrypted string
     * @throws NoSuchAlgorithmException -
     */
    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * get the encryption string after using sha256 algorithm of getSHA method
     * and convert byte[] stream to string.
     *
     * @param password - password to encrypt
     * @return - string after encryption
     * @throws NoSuchAlgorithmException -
     */
    public static String encrypt(String password) throws NoSuchAlgorithmException {

        byte[] hash = getSHA(password);
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
}
