package ch.bbw.pr.tresorbackend.util;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.StringReader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtil {
    public static RSAPublicKey getPublicKeyFromString(String key) throws Exception {
        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded, "RSA");
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(keySpec);
    }

    public static RSAPrivateKey getPrivateKeyFromString(String key) throws Exception {
        // Parse PEM format
        PEMParser pemParser = new PEMParser(new StringReader(key));
        Object object = pemParser.readObject();
        pemParser.close();

        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        var privateKey = converter.getPrivateKey(((PEMKeyPair)object).getPrivateKeyInfo());
        return (RSAPrivateKey)privateKey;
    }
}
