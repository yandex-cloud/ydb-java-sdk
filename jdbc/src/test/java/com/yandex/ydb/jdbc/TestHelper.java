package com.yandex.ydb.jdbc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.yandex.ydb.jdbc.exception.YdbConfigurationException;
import com.yandex.ydb.jdbc.settings.YdbProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestHelper.class);

    private static final String RECIPE_DATABASE_FILE = "ydb_database.txt";
    private static final String RECIPE_ENDPOINT_FILE = "ydb_endpoint.txt";

    private TestHelper() {
        //
    }

    public static String getTestUrl() throws YdbConfigurationException {
        String defaultUrl;
        if (isRecipe()) {
            defaultUrl = String.format("jdbc:ydb:%s/%s", recipeEndpoint(), recipeDatabase());
        } else {
            defaultUrl = "jdbc:ydb:localhost:2135/local"; // Local docker instance
        }
        return System.getProperty("YDB_URL", defaultUrl);
    }

    public static void assertThrowsMsg(Class<? extends Throwable> type, Executable exec, String expectMessage) {
        assertThrowsMsg(type, exec, e ->
                        Assertions.assertEquals(expectMessage, e.getMessage(), "Error message"),
                null);
    }

    public static void assertThrowsMsgLike(Class<? extends Throwable> type, Executable exec, String expectLike) {
        assertThrowsMsgLike(type, exec, expectLike, null);
    }

    public static void assertThrowsMsgLike(Class<? extends Throwable> type, Executable exec, String expectLike,
                                           String description) {
        assertThrowsMsg(type, exec, e -> {
            String errorMessage = e.getMessage();
            Assertions.assertTrue(errorMessage.contains(expectLike),
                    String.format("Error message [%s] must contains [%s]", errorMessage, expectLike));
        }, description);
    }


    public static <T extends Throwable> void assertThrowsMsg(Class<T> type, Executable exec, Consumer<T> check,
                                                             @Nullable String description) {
        T throwable = Assertions.assertThrows(type, exec, description);
        LOGGER.trace("Catch exception", throwable);
        check.accept(throwable);
    }

    //

    public static String recipeEndpoint() throws YdbConfigurationException {
        return YdbProperties.stringFileReference("file:" + RECIPE_ENDPOINT_FILE);
    }

    public static String recipeDatabase() throws YdbConfigurationException {
        return YdbProperties.stringFileReference("file:" + RECIPE_DATABASE_FILE);
    }

    public static String stringFileReference(String reference) {
        try {
            return YdbProperties.stringFileReference(reference);
        } catch (YdbConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isRecipe() {
        return Files.exists(Paths.get(RECIPE_ENDPOINT_FILE));
    }

}
