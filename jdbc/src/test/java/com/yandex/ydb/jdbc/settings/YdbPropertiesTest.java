package com.yandex.ydb.jdbc.settings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import com.yandex.ydb.jdbc.exception.YdbConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;

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

    @ParameterizedTest
    @CsvSource({
        ", null",

        "jdbc:ydb:ydb.net:2135/db?token=token,                  jdbc:ydb:ydb.net:2135/db?token=*****",
        "jdbc:ydb:ydb.net:2135/db?Token=,                       jdbc:ydb:ydb.net:2135/db?Token=*****",
        "jdbc:ydb:ydb.net:2135/db?TOKEN=&mode=one,              jdbc:ydb:ydb.net:2135/db?TOKEN=*****&mode=one",
        "jdbc:ydb:ydb.net:2135/db?TOKEN=val&mode=one,           jdbc:ydb:ydb.net:2135/db?TOKEN=*****&mode=one",
        "jdbc:ydb:ydb.net:2135/db?syntax=v1&token=lll&mode=two, jdbc:ydb:ydb.net:2135/db?syntax=v1&token=*****&mode=two",
    })
    void hideUrlSecrets(String url, String hided) {
        assertEquals(hided, YdbProperties.hideSecrets(url));
    }

    @ParameterizedTest
    @ArgumentsSource(PropertySecretsGenerator.class)
    void hidePropertySecrets(Properties props, String expected) {
        assertEquals(expected, YdbProperties.hideSecrets(props));
    }

    static class PropertySecretsGenerator implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext ec) throws Exception {
            return Stream.of(
                    Arguments.of(null, "null"),
                    Arguments.of(props(), "{}"),
                    Arguments.of(props("key", "value"), "{key=value, }"),
                    Arguments.of(props("token", ""), "{token=*****, }"),
                    Arguments.of(props("key", "value", "TOKEN", "v1"), "{TOKEN=*****, key=value, }"),
                    Arguments.of(props("toKen", "", "key", "1"), "{key=1, toKen=*****, }")
            );
        }

        private static Properties props(String... args) {
            if (args == null) {
                return null;
            }

            Properties props = new Properties();
            for (int idx = 0; idx < args.length - 1; idx += 2) {
                props.setProperty(args[idx], args[idx + 1]);
            }
            return props;
        }
    }
}
