RECURSE_FOR_TESTS(
    src/test
)

JAVA_PROGRAM(ydb-cookbook)

JDK_VERSION(11)
JAVAC_FLAGS(--release 8)

OWNER(g:kikimr)

JAVA_SRCS(SRCDIR src/main/java **/*)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/examples/ya.dependencies.inc)

PEERDIR(
    kikimr/public/sdk/java/table
    kikimr/public/sdk/java/auth-iam

    contrib/java/com/beust/jcommander
)

LINT(extended)
END()
