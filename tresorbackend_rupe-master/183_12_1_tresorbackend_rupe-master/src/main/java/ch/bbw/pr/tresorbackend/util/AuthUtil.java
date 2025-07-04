package ch.bbw.pr.tresorbackend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import java.security.*;
import java.util.Date;

public class AuthUtil {
    private final Algorithm algorithm;

    public AuthUtil(String publicKeyStr, String privateKeyStr) throws Exception {
        System.out.println(publicKeyStr);
        System.out.println(privateKeyStr);

        var pubKey = KeyUtil.getPublicKeyFromString(publicKeyStr);
        var privateKey = KeyUtil.getPrivateKeyFromString(privateKeyStr);

        algorithm = Algorithm.RSA256(pubKey, privateKey);
    }

    public String generateJWT(String email, Role role) throws NoSuchAlgorithmException {
        return JWT.create()
                .withIssuer("http://localhost:8080")
                .withClaim("sub", email)
                .withClaim("role", role.toString())
                .withClaim("iat", new Date())
                .sign(algorithm);
    }

    public JwtPayload getPayloadAndVerifyJWT(String jwt) {
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("http://localhost:8080").build();
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
