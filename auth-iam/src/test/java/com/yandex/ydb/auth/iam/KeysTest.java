package com.yandex.ydb.auth.iam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Sergey Polovko
 */
public class KeysTest {

    @Test
    public void publicKeyFromString() throws IOException {
        String keyString = Resources.toString(getClass().getResource("public.txt"), StandardCharsets.UTF_8);
        PublicKey key = Keys.publicKey(keyString);
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("X.509", key.getFormat());

        try {
            Keys.publicKey("some invalid string");
            fail("expected exception is not thrown");
        } catch (Exception e) {
            assertEquals("cannot read public key from string \"some invalid string\"", e.getMessage());
        }
    }

    @Test
    public void privateKeyFromString() throws IOException {
        String keyString = Resources.toString(getClass().getResource("private.txt"), StandardCharsets.UTF_8);
        PrivateKey key = Keys.privateKey(keyString);
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());

        try {
            Keys.privateKey("another invalid string");
            fail("expected exception is not thrown");
        } catch (Exception e) {
            assertEquals("cannot read private key from string \"another invalid string\"", e.getMessage());
        }
    }

    @Test
    public void publicKeyFromStream() {
        PublicKey key = Keys.publicKey(getClass().getResourceAsStream("public.txt"));
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("X.509", key.getFormat());
    }

    @Test
    public void privateKeyFromStream() {
        PrivateKey key = Keys.privateKey(getClass().getResourceAsStream("private.txt"));
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());
    }

    @Test
    public void publicKeyFromFile() throws IOException {
        File file = copyResourceToTempFile("public.txt");
        PublicKey key = Keys.publicKey(file);
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("X.509", key.getFormat());
    }

    @Test
    public void privateKeyFromFile() throws IOException {
        File file = copyResourceToTempFile("private.txt");
        PrivateKey key = Keys.privateKey(file);
        assertEquals("RSA", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());
    }

    private static File copyResourceToTempFile(String resourceName) throws IOException {
        File tempFile = File.createTempFile("KeysTest", resourceName);
        tempFile.deleteOnExit();

        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            InputStream in = KeysTest.class.getResourceAsStream(resourceName);
            assertTrue(ByteStreams.copy(in, out) > 0);
        }

        return tempFile;
    }
}
