package com.yandex.ydb.auth.iam;

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
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import static com.google.common.base.Preconditions.checkArgument;


/**
 * @author Sergey Polovko
 */
@ParametersAreNonnullByDefault
public final class Keys {
    private Keys() {}

    public static PrivateKey privateKey(InputStream is) {
        try {
            return readPrivate(new InputStreamReader(is));
        } catch (Exception e) {
            throw new RuntimeException("cannot read private key from input stream", e);
        }
    }

    public static PublicKey publicKey(InputStream is) {
        try {
            return readPublic(new InputStreamReader(is));
        } catch (Exception e) {
            throw new RuntimeException("cannot read public key from input stream", e);
        }
    }

    public static PrivateKey privateKey(File file) {
        checkArgument(file.exists(), "file \"%s\" does not exist", file);
        try {
            return readPrivate(new FileReader(file));
        } catch (Exception e) {
            throw new RuntimeException("cannot read private key from file \"" + file + '\"', e);
        }
    }

    public static PublicKey publicKey(File file) {
        checkArgument(file.exists(), "file \"%s\" does not exist", file);
        try {
            return readPublic(new FileReader(file));
        } catch (Exception e) {
            throw new RuntimeException("cannot read public key from file \"" + file + '\"', e);
        }
    }

    public static PrivateKey privateKey(String data) {
        checkArgument(data.startsWith("-----BEGIN "), "cannot read private key from string \"%s\"", data);
        try {
            return readPrivate(new StringReader(data));
        } catch (Exception e) {
            throw new RuntimeException("cannot read private key from string \"" + data + '\"', e);
        }
    }

    public static PublicKey publicKey(String data) {
        checkArgument(data.startsWith("-----BEGIN "), "cannot read public key from string \"%s\"", data);
        try {
            return readPublic(new StringReader(data));
        } catch (Exception e) {
            throw new RuntimeException("cannot read public key from string \"" + data + '\"', e);
        }
    }

    private static PrivateKey readPrivate(Reader in) throws Exception {
        try (PemReader reader = new PemReader(in)) {
            PemObject privateKeyPem = Objects.requireNonNull(reader.readPemObject(), "PEM object was not read");
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
