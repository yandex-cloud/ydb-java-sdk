package com.yandex.ydb.jdbc.settings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import com.yandex.ydb.jdbc.exception.YdbConfigurationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class YdbPropertiesTest {

    @Test
    void resolveFilePath() throws YdbConfigurationException, MalformedURLException {
        Optional<URL> url = YdbProperties.resolvePath("file:/root.file");
        assertEquals(Optional.of(new URL("file:/root.file")), url);
    }

    @Test
    void resolveClassPath() throws YdbConfigurationException {
        Optional<URL> url = YdbProperties.resolvePath("classpath:log4j2.xml");
        URL res = ClassLoader.getSystemResource("log4j2.xml");
        assertNotNull(res);
        assertEquals(Optional.of(res), url);
    }

    @Test
    void resolveFilePathFromHome() throws YdbConfigurationException, MalformedURLException {
        Optional<URL> url = YdbProperties.resolvePath("file:~/home.file");
        String home = System.getProperty("user.home");
        assertEquals(Optional.of(new URL("file:" + home + "/home.file")), url);
    }

    @Test
    void resolveFilePathFromHomePure() throws YdbConfigurationException, MalformedURLException {
        Optional<URL> url = YdbProperties.resolvePath("~/home.file");
        String home = System.getProperty("user.home");
        assertEquals(Optional.of(new URL("file:" + home + "/home.file")), url);
    }
}
