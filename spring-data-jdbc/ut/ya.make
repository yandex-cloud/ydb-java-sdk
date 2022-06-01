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

INCLUDE(${ARCADIA_ROOT}/kikimr/public/tools/ydb_recipe/recipe_stable.inc)

JAVA_SRCS(
    SRCDIR ${ARCADIA_ROOT}/kikimr/public/sdk/java/spring-data-jdbc/src/test/java **/*.java
)
JAVA_SRCS(
    SRCDIR ${ARCADIA_ROOT}/kikimr/public/sdk/java/spring-data-jdbc/src/test/resources **/*
)

PEERDIR(
    kikimr/public/sdk/java/spring-data-jdbc
    kikimr/public/sdk/java/jdbc/ut
    contrib/java/org/springframework/spring-test/5.3.6
)

LINT(base)
END()
