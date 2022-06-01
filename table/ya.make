JAVA_LIBRARY(ydb-table)

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
    kikimr/public/sdk/java/core
)

LINT(base)
END()

RECURSE_FOR_TESTS(
    ut
)
