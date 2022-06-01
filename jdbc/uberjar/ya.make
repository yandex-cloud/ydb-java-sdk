JAVA_PROGRAM()

IF(JDK_VERSION == "")
    JDK_VERSION(11)
ENDIF()
UBERJAR()

OWNER(g:yt)

JAVA_SRCS(
    SRCDIR src/main/resources **/*
)

PEERDIR(
    kikimr/public/sdk/java/jdbc
    contrib/java/org/apache/logging/log4j/log4j-slf4j-impl/2.14.0
)

UBERJAR_PATH_EXCLUDE_PREFIX (META-INF/org/apache/logging/log4j/core/config/plugins/*.dat)

END()
