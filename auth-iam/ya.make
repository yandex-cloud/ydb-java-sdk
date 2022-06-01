JAVA_LIBRARY(ydb-auth-iam)

IF(JDK_VERSION == "")
    JDK_VERSION(11)
ENDIF()
MAVEN_GROUP_ID(com.yandex.ydb.auth.iam)

OWNER(g:kikimr)

JAVA_SRCS(
    SRCDIR src/main/java
    **/*.java
)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/ya.dependencies.inc)

PEERDIR(
    kikimr/public/sdk/java/core

    contrib/java/com/yandex/cloud/java-sdk-auth
)

LINT(base)
END()
