JTEST()

IF(JDK_VERSION == "")
    JDK_VERSION(11)
ENDIF()

TIMEOUT(240)
SIZE(MEDIUM)
OWNER(g:kikimr)

JAVA_SRCS(
    SRCDIR ${ARCADIA_ROOT}/kikimr/public/sdk/java/table/src/test/java
    **/*.java
)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/ya.dependencies.inc)
INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/ya.test_dependencies.inc)

PEERDIR(
    kikimr/public/sdk/java/table

    contrib/java/org/apache/logging/log4j/log4j-slf4j-impl
    contrib/java/com/google/truth/truth
    contrib/java/com/google/truth/extensions/truth-proto-extension
)

LINT(base)
END()
