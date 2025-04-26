package ch.bbw.pr.tresorbackend.service;

import com.google.common.hash.Hashing;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * PasswordEncryptionService
 * @author Peter Rutschmann
 */
@Service
public class PasswordEncryptionService {

   String pepper = "+jb)tN*R?Y@l";

   public PasswordEncryptionService() {}

   public String hashPassword(String password) throws NoSuchAlgorithmException {
      var salt = generateSalt();

      var seasonedPassword = pepper + salt + password;
      String hashedPassword = Hashing.sha256()
              .hashString(seasonedPassword, StandardCharsets.UTF_8)
              .toString();

      return salt + "$" + hashedPassword;
   }

   public String getSaltFromHashedPassword(String hashedPassword) {
      return hashedPassword.split("\\$")[0];
   }

   private String generateSalt() {
      byte[] saltByteArray = new byte[16];
      var random = new SecureRandom();

      String salt;
      do {
         random.nextBytes(saltByteArray);
         salt = new String(saltByteArray, StandardCharsets.UTF_8);

      } while (salt.endsWith("$"));

      System.out.println(salt);
      return salt;
   }
}
