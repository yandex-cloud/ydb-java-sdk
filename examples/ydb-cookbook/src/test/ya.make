JUNIT5(ydb-cookbook-test)

SIZE(MEDIUM)

OWNER(g:kikimr)

JAVA_SRCS(SRCDIR java **/*)
JAVA_SRCS(SRCDIR resources **/*)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/tools/ydb_recipe/recipe_stable.inc)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/examples/ya.dependencies.inc)

PEERDIR(
    kikimr/public/sdk/java/examples/ydb-cookbook

    contrib/java/org/testcontainers/testcontainers
    contrib/java/org/apache/logging/log4j/log4j-slf4j-impl

    contrib/java/org/junit/jupiter/junit-jupiter
    contrib/java/org/junit/vintage/junit-vintage-engine
)

LINT(extended)
END()

