JTEST()

IF(JDK_VERSION == "")
    JDK_VERSION(11)
ENDIF()

OWNER(g:kikimr)

JAVA_SRCS(
    SRCDIR ${ARCADIA_ROOT}/kikimr/public/sdk/java/core/src/test/java
    **/*.java
)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/ya.dependencies.inc)
INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/ya.test_dependencies.inc)

PEERDIR(
    kikimr/public/sdk/java/core

    contrib/java/org/apache/logging/log4j/log4j-slf4j-impl
    contrib/java/junit/junit
    contrib/java/org/mockito/mockito-inline
    contrib/java/com/google/truth/truth
    contrib/java/com/google/truth/extensions/truth-proto-extension
)

LINT(base)
END()
