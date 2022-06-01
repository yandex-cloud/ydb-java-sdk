JAVA_LIBRARY(ydb-core)

IF(JDK_VERSION == "")
    JDK_VERSION(11)
ENDIF()
MAVEN_GROUP_ID(com.yandex.ydb)

OWNER(g:kikimr)

JAVA_SRCS(
    SRCDIR src/main/java
    **/*.java
)

JAVA_SRCS(
    SRCDIR src/main/resources
    **/*
)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/ya.dependencies.inc)

PEERDIR(
    contrib/java/io/grpc/grpc-netty
    contrib/java/org/slf4j/slf4j-api
    ydb/public/api/grpc
    ydb/public/api/grpc/draft
)

LINT(base)

END()
