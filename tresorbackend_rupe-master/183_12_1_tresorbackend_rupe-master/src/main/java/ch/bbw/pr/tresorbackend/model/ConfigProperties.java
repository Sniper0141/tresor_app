package ch.bbw.pr.tresorbackend.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Scanner;

/**
 * ConfigProperties
 * @author Peter Rutschmann
 */
@Component
public class ConfigProperties {

   @Value("${CROSS_ORIGIN}")
   private String crossOrigin;

   public String getOrigin() {
      return crossOrigin;
   }

    public String getMasterKey() throws IOException {
      var secretFile = new File("C:\\Development\\Schule\\Modul_183\\secrets\\tresor_app\\tresor_app_master_key.txt");
      var scanner = new Scanner(secretFile);

      StringBuilder result = new StringBuilder();
      while (scanner.hasNextLine()) {
         var line = scanner.nextLine();
         result.append(line).append("\n");
      }

      return result.toString();
    }
}
