JAVA_LIBRARY(ydb-jdbc)

IF(JDK_VERSION == "")
    JDK_VERSION(11)
ENDIF()
MAVEN_GROUP_ID(com.yandex.ydb)

OWNER(g:kikimr)

JAVA_SRCS(
    SRCDIR src/main/java **/*.java
)
JAVA_SRCS(
    SRCDIR src/main/resources **/*
)

PEERDIR(
    kikimr/public/sdk/java/table
    library/java/svnversion
)

LINT(base)
END()

RECURSE(
    uberjar
)

RECURSE_FOR_TESTS(
    ut
)
