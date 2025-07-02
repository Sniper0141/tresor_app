package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.*;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptionService;
import ch.bbw.pr.tresorbackend.service.UserService;

import ch.bbw.pr.tresorbackend.util.AuthUtil;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UserController
 * @author Peter Rutschmann
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/users")
public class UserController {

    private UserService userService;
    private PasswordEncryptionService passwordService;
    private final ConfigProperties configProperties;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Autowired
    public UserController(ConfigProperties configProperties, UserService userService, PasswordEncryptionService passwordService) {
        this.configProperties = configProperties;
        System.out.println("UserController.UserController: cross origin: " + configProperties.getOrigin());
        // Logging in the constructor
        logger.info("UserController initialized: " + configProperties.getOrigin());
        logger.debug("UserController.UserController: Cross Origin Config: {}", configProperties.getOrigin());
        this.userService = userService;
        this.passwordService = passwordService;
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping("login")
    public ResponseEntity<String> doLoginUser(
            @RequestBody LoginUser loginUser,
            @CookieValue(name = "jwt", required = false) String jwt,
            BindingResult bindingResult) {
        //validate-jwt
        var statusCode = validateJwt(jwt, false, null);
        if(statusCode != null){
            return ResponseEntity.status(statusCode).body(null);
        }

        //input validation
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .toList();
            System.out.println("UserController.doLoginUser " + errors);

            JsonArray arr = new JsonArray();
            errors.forEach(arr::add);
            JsonObject obj = new JsonObject();
            obj.add("message", arr);
            String json = new Gson().toJson(obj);

            System.out.println("UserController.doLoginUser, validation fails: " + json);
            return ResponseEntity.badRequest().body(json);
        }

        System.out.println("UserController.doLoginUser: input validation passed");

        var user = userService.findByEmail(loginUser.getEmail());
        if(user == null){
            logger.info("UserController.doLoginUser: User not found with email: " + loginUser.getEmail());
            return getWrongEmailOrPasswordResponse(0);
        }

        var actualPassword = user.getPassword();
        var loginPassword = loginUser.getPassword();
        if(!passwordService.doPasswordsMatch(loginPassword, actualPassword)){
            logger.info("UserController.doLoginUser: Passwords do not match");
            return getWrongEmailOrPasswordResponse(user.getId());
        }

        logger.info("UserController.doLoginUser: Login passed");

        return getValidLoginResponse(loginUser, user);
    }

    @Value("${RECAPTCHA_PRIVATE_KEY}")
    private String reCaptchaPrivateKey;

    // build create User REST API
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody RegisterUser registerUser, @CookieValue(name = "jwt", required = false) String jwt, BindingResult bindingResult) {
        //validate-jwt
        var statusCode = validateJwt(jwt, false, null);
        if(statusCode != null && statusCode != 200){
            return ResponseEntity.status(statusCode).body(null);
        }

        //input validation
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .toList();
            return validateInput(errors);
        }
        System.out.println("UserController.createUser: input validation passed");

        // password validation
        var errorString = validatePassword(registerUser.getPassword());
        if (!errorString.isEmpty()) {

            JsonObject obj = new JsonObject();
            obj.addProperty("answer", errorString);
            String json = new Gson().toJson(obj);

            return ResponseEntity.badRequest().body(json);
        }

        System.out.println("UserController.createUser, password validation passed");

        // captcha validation
        if(reCaptchaPrivateKey == null){
            JsonObject obj = new JsonObject();
            obj.addProperty("answer", "Unexpected Server Error");
            String json = new Gson().toJson(obj);

            return ResponseEntity.badRequest().body(json);
        }
        var VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
        var restTemplate = new RestTemplate();
        var url = VERIFY_URL + "?secret=" + reCaptchaPrivateKey + "&response=" + registerUser.getRecaptchaToken();

        Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
        if(!(Boolean)response.get("success")){
            JsonObject obj = new JsonObject();
            obj.addProperty("answer", "Captcha needs to be verified.");
            String json = new Gson().toJson(obj);
            return ResponseEntity.badRequest().body(json);
        }
        System.out.println("UserController.createUser: captcha passed.");


        //transform registerUser to user
        User user = new User(
            null,
            registerUser.getFirstName(),
            registerUser.getLastName(),
            registerUser.getEmail(),
            passwordService.encryptPassword(registerUser.getPassword()));

        try{
            userService.createUser(user);
        }
        catch (Exception e){
            ResponseEntity.internalServerError().body("Failed to create user.");
        }

        System.out.println("UserController.createUser, user saved in db");
        return ResponseEntity.accepted().body(null);
    }

    // build get user by id REST API
    // http://localhost:8080/api/users/1
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping("{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long userId, @CookieValue(name = "jwt", required = false) String jwt) {
        //validate-jwt
        var statusCode = validateJwt(jwt, false, null);
        if(statusCode != null && statusCode != 200){
            return ResponseEntity.status(statusCode).body(null);
        }

        User user = userService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // Build Get All Users REST API
    // http://localhost:8080/api/users
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@CookieValue(name = "jwt") String jwt) {
        //validate-jwt
        var statusCode = validateJwt(jwt, true, null);
        if(statusCode != null && statusCode != 200){
            return ResponseEntity.status(statusCode).body(null);
        }

        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // Build Update User REST API
    // http://localhost:8080/api/users/1
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PutMapping("{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable("id") Long userId,
            @RequestBody User user,
            @CookieValue(name = "jwt") String jwt) {
        //validate-jwt
        var statusCode = validateJwt(jwt, false, user.getEmail());
        if(statusCode != null && statusCode != 200){
            return ResponseEntity.status(statusCode).body(null);
        }

        user.setId(userId);
        User updatedUser = userService.updateUser(user);
        return new ResponseEntity<>(updatedUser.toString(), HttpStatus.OK);
    }

    // Build Delete User REST API
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable("id") Long userId,
            @CookieValue(name = "jwt") String jwt) {
        //validate-jwt
        var user = userService.getUserById(userId);
        var statusCode = validateJwt(jwt, false, user.getEmail());
        if(statusCode != null && statusCode != 200){
            return ResponseEntity.status(statusCode).body(null);
        }

        userService.deleteUser(userId);
        return new ResponseEntity<>("User successfully deleted!", HttpStatus.OK);
    }


    // get user id by email
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping("/byemail")
    public ResponseEntity<String> getUserIdByEmail(
            @RequestBody EmailAdress email,
            @CookieValue(name = "jwt") String jwt,
            BindingResult bindingResult) {
        //validate-jwt
        var statusCode = validateJwt(jwt, true, null);
        if(statusCode != null && statusCode != 200){
            return ResponseEntity.status(statusCode).body(null);
        }

        System.out.println("UserController.getUserIdByEmail: " + email);
        //input validation
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.toList());
            return validateInput(errors);
        }

        System.out.println("UserController.getUserIdByEmail: input validation passed");

        User user = userService.findByEmail(email.getEmail());
        if (user == null) {
            System.out.println("UserController.getUserIdByEmail, no user found with email: " + email);
            JsonObject obj = new JsonObject();
            obj.addProperty("message", "No user found with this email");
            String json = new Gson().toJson(obj);

            System.out.println("UserController.getUserIdByEmail, fails: " + json);
            return ResponseEntity.badRequest().body(json);
        }
        System.out.println("UserController.getUserIdByEmail, user find by email");
        JsonObject obj = new JsonObject();
        obj.addProperty("answer", user.getId());
        String json = new Gson().toJson(obj);
        System.out.println("UserController.getUserIdByEmail " + json);
        return ResponseEntity.accepted().body(json);
    }

    private ResponseEntity<String> validateInput(List<String> errors) {
        System.out.println("UserController.createUser " + errors);

        JsonArray arr = new JsonArray();
        errors.forEach(arr::add);
        JsonObject obj = new JsonObject();
        obj.add("message", arr);
        String json = new Gson().toJson(obj);

        System.out.println("UserController.createUser, validation fails: " + json);
        return ResponseEntity.badRequest().body(json);
    }

    private String validatePassword(String password) {
        var errorString = "";

        if(!password.matches(".*[a-z].*")){
            errorString += "Must contain lowercase letters.\n";
        }
        if(!password.matches(".*[A-Z].*")){
            errorString += "Must contain uppercase letters.\n";
        }
        if(!password.matches(".*[0-9].*")){
            errorString += "Must contain numbers.\n";
        }
        if(password.length() < 8){
            errorString += "Must be at least 8 characters.\n";
        }

        return errorString;
    }

    private ResponseEntity<String> getWrongEmailOrPasswordResponse(long userId){
        var response = new LoginResponse("Email or password incorrect.", userId);
        var responseJson = new Gson().toJson(response);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseJson);
    }

    private ResponseEntity<String> getValidLoginResponse(LoginUser loginUser, User user){
        AuthUtil authUtil;
        try{
            authUtil = new AuthUtil();
        }
        catch(Exception e){
            logger.error("UserController.doLoginUser: AuthUtil exception: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Something went wrong");
        }

        var role = loginUser.getEmail().equalsIgnoreCase("admin@email.com")
                ? AuthUtil.Role.Admin
                : AuthUtil.Role.User;

        String jwt;
        try{
            jwt = authUtil.generateJWT(loginUser.getEmail(), role);
        } catch (NoSuchAlgorithmException | JWTCreationException e) {
            logger.error("UserController.doLoginUser: Failed to generate JWT. {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Something went wrong");
        }

        var headers = new HttpHeaders();
        headers.add("Set-Cookie", "token=" + jwt + "; Path=/; HttpOnly; Max-Age=86400");

        var responseObj = new LoginResponse("Login successful.", user.getId());
        var responseBody = new Gson().toJson(responseObj);

        return ResponseEntity.ok().headers(headers).body(responseBody);
    }

    private Integer validateJwt(String jwt, boolean adminNeeded, String permittedUser){
        if(jwt == null || jwt.isEmpty()){
            return null;
        }

        AuthUtil.JwtPayload jwtPayload;
        try{
            var authUtil = new AuthUtil();
            jwtPayload = authUtil.getPayloadAndVerifyJWT(jwt);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
            return 500;
        } catch (JWTVerificationException e){
            logger.warn("This JWT is not valid...");
            return 400;
        }

        // Check if expired
        try{
            var issuedAt = Integer.parseInt(jwtPayload.iat());
            var epochSecondsYesterday = Instant.now().minusSeconds(86400).getEpochSecond();

            if(issuedAt < epochSecondsYesterday){
                logger.warn("JWT is expired.");
                return null;
            }
        }
        catch(NumberFormatException e){
            logger.error(e.getMessage());
            return 500;
        }

        // check correct user
        if(permittedUser != null){
            return jwtPayload.sub().equalsIgnoreCase(permittedUser)
                    ? null
                    : 403;
        }

        // check admin
        if(adminNeeded){
            return jwtPayload.role().equals("Admin")
                    ? null
                    : 403;
        }

        return 200;
    }
}