JUNIT5()

IF(JDK_VERSION == "")
    JDK_VERSION(11)
ENDIF()

# Default timeout of 10 minutes
# FIXME Discrepancy between the available test versions by dependencies with the launcher version was found.
# Remove ENV macro below to reproduce the problem.
# For more info see https://st.yandex-team.ru/DEVTOOLSSUPPORT-7454#6128ec627e6507138f034e45
ENV(DISABLE_JUNIT_COMPATIBILITY_TEST=1)

# Default timeout of 10 minutes
SIZE(MEDIUM)
OWNER(g:kikimr)

ENV(YDB_YQL_SYNTAX_VERSION="0")
INCLUDE(${ARCADIA_ROOT}/kikimr/public/tools/ydb_recipe/recipe_stable.inc)

JAVA_SRCS(
    SRCDIR ${ARCADIA_ROOT}/kikimr/public/sdk/java/jdbc/src/test/java **/*.java
)
JAVA_SRCS(
    SRCDIR ${ARCADIA_ROOT}/kikimr/public/sdk/java/jdbc/src/test/resources **/*
)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/ya.dependencies.inc)
INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/ya.test_dependencies.inc)

PEERDIR(
    kikimr/public/sdk/java/jdbc
    contrib/java/org/apache/logging/log4j/log4j-slf4j-impl
    contrib/java/org/testcontainers/testcontainers
)

LINT(base)
END()
