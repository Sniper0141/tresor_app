package ch.bbw.pr.tresorbackend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import java.security.*;
import java.util.Date;
import java.util.Map;

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

    public Map<String, Claim> getPayloadAndVerifyJWT(String jwt) {
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("http://localhost:8080").build();
        var decodedJWT = verifier.verify(jwt);
        return decodedJWT.getClaims();
    }

    public enum Role{
        User,
        Admin
    }
}
