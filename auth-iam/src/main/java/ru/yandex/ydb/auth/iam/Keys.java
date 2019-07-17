package ru.yandex.ydb.auth.iam;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;


/**
 * @author Sergey Polovko
 */
public final class Keys {
    private Keys() {}

    public static PrivateKey privateKey(InputStream is) {
        try {
            return readPrivate(new InputStreamReader(is));
        } catch (Exception e) {
            throw new RuntimeException("cannot read private key input stream", e);
        }
    }

    public static PublicKey publicKey(InputStream is) {
        try {
            return readPublic(new InputStreamReader(is));
        } catch (Exception e) {
            throw new RuntimeException("cannot read private key input stream", e);
        }
    }

    public static PrivateKey privateKey(File file) {
        try {
            return readPrivate(new FileReader(file));
        } catch (Exception e) {
            throw new RuntimeException("cannot read private key from file " + file, e);
        }
    }

    public static PublicKey publicKey(File file) {
        try {
            return readPublic(new FileReader(file));
        } catch (Exception e) {
            throw new RuntimeException("cannot read private key from file " + file, e);
        }
    }

    public static PrivateKey privateKey(String data) {
        try {
            return readPrivate(new StringReader(data));
        } catch (Exception e) {
            throw new RuntimeException("cannot read private from " + data, e);
        }
    }

    public static PublicKey publicKey(String data) {
        try {
            return readPublic(new StringReader(data));
        } catch (Exception e) {
            throw new RuntimeException("cannot read private from " + data, e);
        }
    }

    private static PrivateKey readPrivate(Reader in) throws Exception {
        try (PemReader reader = new PemReader(in)) {
            PemObject privateKeyPem = reader.readPemObject();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyPem.getContent()));
        }
    }

    private static PublicKey readPublic(Reader in) throws Exception {
        try (PemReader reader = new PemReader(in)) {
            PemObject privateKeyPem = reader.readPemObject();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(privateKeyPem.getContent()));
        }
    }
}
