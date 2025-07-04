package ch.bbw.pr.tresorbackend.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * EncryptUtil
 * Used to encrypt content.
 * Not implemented yet.
 * @author Peter Rutschmann
 */
public class EncryptUtil {

    private final String publicKey;
    private final String privateKey;

   public EncryptUtil(String publicKey, String privateKey) {
       this.publicKey = publicKey;
       this.privateKey = privateKey;
   }

   public String encrypt(String data) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());

      var salt = generateSalt();
      var dataToEncrypt = salt + "$" + data;
      byte[] bytes = cipher.doFinal(dataToEncrypt.getBytes(StandardCharsets.UTF_8));

      return new String(Base64.getEncoder().encode(bytes));
   }

   public String decrypt(String data) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());

      var encResult = new String(cipher.doFinal(Base64.getDecoder().decode(data))) ;
      return getSecretDataFromDecryptedData(encResult);
   }

   private String getSecretDataFromDecryptedData(String secretData){
      return secretData.split("\\$")[1];
   }

   private String generateSalt() {
      byte[] saltByteArray = new byte[16];
      var random = new SecureRandom();

      String salt;
      do {
         random.nextBytes(saltByteArray);
         salt = new String(saltByteArray, StandardCharsets.UTF_8);

      } while (salt.contains("$"));

      System.out.println(salt);
      return salt;
   }

   private PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
      byte[] publicKeyBytes = publicKey.getBytes(StandardCharsets.UTF_8);
      var publicKeyFactory = KeyFactory.getInstance("RSA");
      var publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
      return publicKeyFactory.generatePublic(publicKeySpec);
   }

   private PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
      byte[] privateKeyBytes = privateKey.getBytes(StandardCharsets.UTF_8);
      KeyFactory privateKeyFactory = KeyFactory.getInstance("RSA");
      EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
      return privateKeyFactory.generatePrivate(privateKeySpec);
   }
}
