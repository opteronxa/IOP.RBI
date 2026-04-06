package process;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author ByXanum
 */

public class C_Criptor {  // === Cifrado/descifrado 3DES ECB Base64 ===
  
    public String encrypt3DES_ECB_Base64(String _text, byte[] _keyBytes) throws Exception {
        SecretKeySpec y_key = new SecretKeySpec(_keyBytes, "DESede");
        Cipher y_cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        y_cipher.init(Cipher.ENCRYPT_MODE, y_key);
        byte[] y_enc = y_cipher.doFinal(_text.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(y_enc);
    }

    public String decrypt3DES_ECB_Base64(String _base64text, byte[] _keyBytes) throws Exception {
        SecretKeySpec y_key = new SecretKeySpec(_keyBytes, "DESede");
        Cipher y_cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        y_cipher.init(Cipher.DECRYPT_MODE, y_key);
        byte[] dec = y_cipher.doFinal(Base64.getDecoder().decode(_base64text));
        return new String(dec, "UTF-8");
    }

}
