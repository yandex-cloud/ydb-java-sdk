JUNIT5(ydb-url-shortener-demo-test)

JDK_VERSION(11)
JAVAC_FLAGS(--release 8)

SIZE(MEDIUM)

OWNER(g:kikimr)

JAVA_SRCS(SRCDIR java **/*)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/tools/ydb_recipe/recipe_stable.inc)
INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/examples/ya.dependencies.inc)

PEERDIR(
    kikimr/public/sdk/java/examples/url-shortener-demo

    contrib/java/org/testcontainers/testcontainers

    contrib/java/org/junit/jupiter/junit-jupiter
    contrib/java/org/junit/vintage/junit-vintage-engine
)

LINT(extended)
END()
