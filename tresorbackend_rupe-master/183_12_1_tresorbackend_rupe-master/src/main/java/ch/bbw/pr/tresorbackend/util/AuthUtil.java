package ch.bbw.pr.tresorbackend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

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

    public String generateJWT(String email, Role role) throws NoSuchAlgorithmException {
        return JWT.create()
                .withIssuer("auth0")
                .withClaim("sub", email)
                .withClaim("role", role.toString())
                .withClaim("iat", new Date())
                .sign(algorithm);
    }

    public JwtPayload getPayloadAndVerifyJWT(String jwt) {
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
        var decodedJWT = verifier.verify(jwt);
        var payloadStr = decodedJWT.getPayload();
        return new Gson().fromJson(payloadStr, JwtPayload.class);
    }

    public enum Role{
        User,
        Admin
    }

    public record JwtPayload(String sub, String role, String iat) {}
}
