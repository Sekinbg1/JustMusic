package ly.jj.newjustpiano.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    public static byte[] hash(byte[] str) {
        MessageDigest messageDigest;
        byte[] encodeStr = new byte[0];
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str);
            encodeStr = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }
}
