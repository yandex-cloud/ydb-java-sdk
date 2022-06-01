JAVA_LIBRARY(ydb-spring-data-jdbc)

IF(JDK_VERSION == "")
    JDK_VERSION(11)
ENDIF()
MAVEN_GROUP_ID(com.yandex.ydb)

OWNER(g:kikimr)

JAVA_SRCS(
    SRCDIR src/main/java
    **/*.java
)

PEERDIR(
    kikimr/public/sdk/java/jdbc
    contrib/java/org/springframework/data/spring-data-jdbc/2.1.8
)

LINT(base)
END()

RECURSE_FOR_TESTS(
    ut
)
