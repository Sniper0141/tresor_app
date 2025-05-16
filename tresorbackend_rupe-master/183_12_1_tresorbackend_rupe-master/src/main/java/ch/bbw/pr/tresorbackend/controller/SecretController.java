package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.Secret;
import ch.bbw.pr.tresorbackend.model.NewSecret;
import ch.bbw.pr.tresorbackend.model.EncryptCredentials;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.service.MasterKeyService;
import ch.bbw.pr.tresorbackend.service.SecretService;
import ch.bbw.pr.tresorbackend.service.UserService;
import ch.bbw.pr.tresorbackend.util.EncryptUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SecretController
 * @author Peter Rutschmann
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/secrets")
public class SecretController {

   private SecretService secretService;
   private UserService userService;
   private MasterKeyService masterKeyService;

   private EncryptUtil encryptUtil;
   private static final Logger logger = LoggerFactory.getLogger(MasterKeyService.class);

   public SecretController(){
      try{
         encryptUtil = new EncryptUtil(masterKeyService.getMasterKey());
      }
      catch(FileNotFoundException e){
         logger.error(e.getMessage());
      }
   }

   // create secret REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping
   public ResponseEntity<String> createSecret2(@Valid @RequestBody NewSecret newSecret, BindingResult bindingResult) {
      //input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.toList());
         System.out.println("SecretController.createSecret " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("SecretController.createSecret, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }
      System.out.println("SecretController.createSecret, input validation passed");

      User user = userService.findByEmail(newSecret.getEmail());

      //transfer secret and encrypt content
      Secret secret;
      try{
         secret = new Secret(
                 null,
                 user.getId(),
                 encryptUtil.encrypt(newSecret.getContent().toString())
         );
      }
      catch (Exception e){
         logger.error(e.getMessage());
         return ResponseEntity.internalServerError().body("Internal Server Error (500)");
      }
      //save secret in db
      secretService.createSecret(secret);
      System.out.println("SecretController.createSecret, secret saved in db");
      JsonObject obj = new JsonObject();
      obj.addProperty("answer", "Secret saved");
      String json = new Gson().toJson(obj);
      System.out.println("SecretController.createSecret " + json);
      return ResponseEntity.accepted().body(json);
   }

   // Build Get Secrets by userId REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byuserid")
   public ResponseEntity<List<Secret>> getSecretsByUserId(@RequestBody EncryptCredentials credentials) {
      System.out.println("SecretController.getSecretsByUserId " + credentials);

      List<Secret> secrets = secretService.getSecretsByUserId(credentials.getUserId());
      if (secrets.isEmpty()) {
         System.out.println("SecretController.getSecretsByUserId secret isEmpty");
         return ResponseEntity.notFound().build();
      }
      //Decrypt content
      for(Secret secret: secrets) {
         try {
            secret.setContent(encryptUtil.decrypt(secret.getContent()));
         }
         catch (EncryptionOperationNotPossibleException e) {
            System.out.println("SecretController.getSecretsByUserId " + e + " " + secret);
            secret.setContent("not encryptable. Wrong password?");
         }
         catch (NoSuchAlgorithmException | InvalidKeySpecException |
                IOException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException e){
            System.out.println("SecretController.getSecretsByUserId " + e + " " + secret);
            secret.setContent("Failed to decrypt this secret, try again...");
         }
      }

      System.out.println("SecretController.getSecretsByUserId " + secrets);
      return ResponseEntity.ok(secrets);
   }

   // Build Get Secrets by email REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byemail")
   public ResponseEntity<List<Secret>> getSecretsByEmail(@RequestBody EncryptCredentials credentials) {
      System.out.println("SecretController.getSecretsByEmail " + credentials);

      User user = userService.findByEmail(credentials.getEmail());

      List<Secret> secrets = secretService.getSecretsByUserId(user.getId());
      if (secrets.isEmpty()) {
         System.out.println("SecretController.getSecretsByEmail secret isEmpty");
         return ResponseEntity.notFound().build();
      }
      //Decrypt content
      for(Secret secret: secrets) {
         try {
            secret.setContent(encryptUtil.decrypt(secret.getContent()));
         }
         catch (EncryptionOperationNotPossibleException e) {
            System.out.println("SecretController.getSecretsByEmail " + e + " " + secret);
            secret.setContent("not encryptable. Wrong password?");
         }
         catch (NoSuchAlgorithmException | InvalidKeySpecException |
                IOException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException e){
            System.out.println("SecretController.getSecretsByEmail " + e + " " + secret);
            secret.setContent("Failed to decrypt this secret, try again...");
         }
      }

      System.out.println("SecretController.getSecretsByEmail " + secrets);
      return ResponseEntity.ok(secrets);
   }

   // Build Get All Secrets REST API
   // http://localhost:8080/api/secrets
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @GetMapping
   public ResponseEntity<List<Secret>> getAllSecrets() {
      List<Secret> secrets = secretService.getAllSecrets();
      return new ResponseEntity<>(secrets, HttpStatus.OK);
   }

   // Build Update Secrete REST API
   // http://localhost:8080/api/secrets/1
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PutMapping("{id}")
   public ResponseEntity<String> updateSecret(
         @PathVariable("id") Long secretId,
         @Valid @RequestBody NewSecret newSecret,
         BindingResult bindingResult) {
      //input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.toList());
         System.out.println("SecretController.createSecret " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("SecretController.updateSecret, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }

      //get Secret with id
      Secret dbSecret = secretService.getSecretById(secretId);
      if(dbSecret == null){
         System.out.println("SecretController.updateSecret, secret not found in db");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Secret not found in db");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret failed:" + json);
         return ResponseEntity.badRequest().body(json);
      }
      User user = userService.findByEmail(newSecret.getEmail());

      //check if Secret in db has not same userid
      if(dbSecret.getUserId() != user.getId()){
         System.out.println("SecretController.updateSecret, not same user id");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Secret has not same user id");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret failed:" + json);
         return ResponseEntity.badRequest().body(json);
      }
      //check if Secret can be decrypted with password
      try {
         encryptUtil.decrypt(dbSecret.getContent());
      }
      catch (EncryptionOperationNotPossibleException e) {
         System.out.println("SecretController.updateSecret, invalid password");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Password not correct.");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret failed:" + json);
         return ResponseEntity.badRequest().body(json);
      }
      catch (NoSuchAlgorithmException | InvalidKeySpecException |
             IOException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
             BadPaddingException e){
         System.out.println("SecretController.updateSecret " + e + " " + dbSecret);

         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Internal Server Error (500)");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret " + json);
         return ResponseEntity.internalServerError().body(json);
      }

      //modify Secret in db.
      Secret secret;
      try {
         secret = new Secret(
                 secretId,
                 user.getId(),
                 encryptUtil.encrypt(newSecret.getContent().toString())
         );
         var updatedSecret = secretService.updateSecret(secret);
      }
      catch (NoSuchAlgorithmException | InvalidKeySpecException |
             IOException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
             BadPaddingException e){
         System.out.println("SecretController.updateSecret " + e + " " + newSecret);

         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Internal Server Error (500)");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret " + json);
         return ResponseEntity.internalServerError().body(json);
      }

      //save secret in db
      secretService.createSecret(secret);
      System.out.println("SecretController.updateSecret, secret updated in db");
      JsonObject obj = new JsonObject();
      obj.addProperty("answer", "Secret updated");
      String json = new Gson().toJson(obj);
      System.out.println("SecretController.updateSecret " + json);
      return ResponseEntity.accepted().body(json);
   }

   // Build Delete Secret REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @DeleteMapping("{id}")
   public ResponseEntity<String> deleteSecret(@PathVariable("id") Long secretId) {
      //todo: Some kind of brute force delete, perhaps test first userid and encryptpassword
      secretService.deleteSecret(secretId);
      System.out.println("SecretController.deleteSecret succesfully: " + secretId);
      return new ResponseEntity<>("Secret successfully deleted!", HttpStatus.OK);
   }
}
