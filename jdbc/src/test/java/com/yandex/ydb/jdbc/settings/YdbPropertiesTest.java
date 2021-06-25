package com.yandex.ydb.jdbc.settings;

import java.io.File;
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
        assertEquals(Optional.of(new File("/root.file").toURI().toURL()), url);
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
        assertEquals(Optional.of(new File(home + "/home.file").toURI().toURL()), url);
    }

    @Test
    void resolveFilePathFromHomePure() throws YdbConfigurationException, MalformedURLException {
        Optional<URL> url = YdbProperties.resolvePath("~/home.file");
        String home = System.getProperty("user.home");
        assertEquals(Optional.of(new File(home + "/home.file").toURI().toURL()), url);
    }
}
