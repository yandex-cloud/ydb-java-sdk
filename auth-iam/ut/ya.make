IF (NOT HARDENING AND NOT SANITIZER_TYPE)
JTEST()

IF(JDK_VERSION == "")
    JDK_VERSION(11)
ENDIF()

OWNER(g:kikimr)
TIMEOUT(180)
SIZE(MEDIUM)

JAVA_SRCS(
    SRCDIR ${ARCADIA_ROOT}/kikimr/public/sdk/java/auth-iam/src/test/java
    **/*.java
)

JAVA_SRCS(
    SRCDIR ${ARCADIA_ROOT}/kikimr/public/sdk/java/auth-iam/src/test/resources
    **/*
)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/ya.dependencies.inc)
INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/ya.test_dependencies.inc)

PEERDIR(
    kikimr/public/sdk/java/auth-iam

    contrib/java/junit/junit
    contrib/java/org/mock-server/mockserver-netty
    contrib/java/org/apache/logging/log4j/log4j-slf4j-impl
)

LINT(base)
END()
ENDIF()
