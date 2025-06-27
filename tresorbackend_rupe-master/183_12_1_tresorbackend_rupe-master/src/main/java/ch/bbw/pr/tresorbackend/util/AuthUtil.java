package ch.bbw.pr.tresorbackend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class AuthUtil {
    private final Algorithm algorithm;

    public AuthUtil() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        algorithm = Algorithm.RSA256(publicKey, privateKey);
    }

    public String generateJWT(String email, String password, Role role) throws NoSuchAlgorithmException {
        return JWT.create()
                .withIssuer("auth0")
                .withClaim("email", email)
                .withClaim("password", password)
                .withClaim("role", role.toString())
                .sign(algorithm);
    }

    public enum Role{
        User,
        Admin
    }
}
