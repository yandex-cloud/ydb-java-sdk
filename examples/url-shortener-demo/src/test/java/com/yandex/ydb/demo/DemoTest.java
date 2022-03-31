package com.yandex.ydb.demo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

/**
 *
 * @author Alexandr Gorshenin
 */
@DisabledIfSystemProperty(named = "DISABLE_INTEGRATION_TESTS", matches = "TRUE")
public class DemoTest {
    private static final Logger log = LoggerFactory.getLogger(DemoTest.class);

    private static Application app = null;
    private static YdbDockerContainer container = null;
    private static URI appURI;

    private static byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, os, 1024);
        return os.toByteArray();
    }

    private static byte[] readResource(String path) {
        try (InputStream is = DemoTest.class.getClassLoader().getResourceAsStream(path)) {
            return readFully(is);
        } catch (IOException e) {
            log.error("can't read resource {}", path, e);
            return null;
        }
    }

    private static byte[] readIndexHtml() {
        return readResource("webapp/index.html");
    }

    @BeforeAll
    public static void setUpYDB() throws Exception {
        PortsGenerator portGenerator = new PortsGenerator();

        String ydbDatabase = System.getenv("YDB_DATABASE");
        String ydbEndpoint = System.getenv("YDB_ENDPOINT");

        int testPort = portGenerator.findAvailablePort();
        List<String> args = new ArrayList<>();
        args.add("-p");
        args.add(String.valueOf(testPort));

        if (ydbEndpoint != null) {
            log.info("set up reciept YDB instance -e {} -d {}", ydbEndpoint, ydbDatabase);
            args.addAll(Arrays.asList("-e", ydbEndpoint, "-d", ydbDatabase));
        } else {
            log.info("set up YDB docker container");
            container = YdbDockerContainer.createAndStart(portGenerator);
            args.addAll(Arrays.asList(
                    "-e", container.nonSecureEndpoint(),
                    "-d", container.database()
            ));
        }

        app = new Application(AppParams.parseArgs(args.toArray(new String[0])));
        app.start();

        appURI = URI.create("http://localhost:" + String.valueOf(testPort));
    }

    @AfterAll
    public static void tearDownYDB() throws InterruptedException {
        app.close();
        app.join();

        if (container != null) {
            log.info("tear down YDB docker container");
            container.stop();
        }
    }

    @Test
    public void testCreateShortUrl() throws IOException, InterruptedException {
        // Check that GET / return index.html
        String index = httpGET("");
        Assertions.assertArrayEquals(readIndexHtml(), index.getBytes(), "index.html body incorrect");

        // Send invalid payload to /url
        httpPOST("/url", null, 400, "check payload validation - null");
        httpPOST("/url", "{}", 400, "check payload validation - empty");
        httpPOST("/url", "{ 'sourc': '" + appURI + "'}", 400, "check payload validation - syntax");

        // Send request to short current app url
        String created = httpPOST("/url", "{ 'source': '" + appURI + "'}");
        JsonElement json = JsonParser.parseString(created);

        Assertions.assertNotNull(json, "empty json response");
        Assertions.assertTrue(json.isJsonObject() && json.getAsJsonObject().has("hash"), "invalid json response");

        String hash = json.getAsJsonObject().get("hash").getAsString();

        // Create and send hash request to redirect to index.html
        httpGETRedirect("/" + hash, appURI.toString());
    }

    private String httpGET(String path) throws IOException, InterruptedException {
        URL url = appURI.resolve(path).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        try {
            Assertions.assertEquals(200, conn.getResponseCode(), "response wrong " + path);
            return new String(readFully(conn.getInputStream()), StandardCharsets.UTF_8);
        } finally {
            conn.disconnect();
        }
    }

    private void httpGETRedirect(String path, String target) throws IOException, InterruptedException {
        URL url = appURI.resolve(path).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(false);
        conn.connect();

        String location = conn.getHeaderField("location");
        Assertions.assertEquals(302, conn.getResponseCode(), "response wrong " + path);
        Assertions.assertEquals(target, location, "redirect target");
        conn.disconnect();
    }

    private void httpPOST(String path, String body, int statusCode, String msg)
            throws IOException, InterruptedException {
        URL url = appURI.resolve(path).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        if (body != null) {
            conn.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(body.getBytes());
            }
        }
        conn.connect();
        Assertions.assertEquals(statusCode, conn.getResponseCode(), msg);
        conn.disconnect();
    }

    private String httpPOST(String path, String body) throws IOException, InterruptedException {
        URL url = appURI.resolve(path).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        if (body != null) {
            conn.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(body.getBytes());
            }
        }
        conn.connect();

        try {
            Assertions.assertEquals(200, conn.getResponseCode(), "response wrong " + path);
            return new String(readFully(conn.getInputStream()), StandardCharsets.UTF_8);
        } finally {
            conn.disconnect();
        }
    }
}
