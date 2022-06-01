RECURSE_FOR_TESTS(
    src/test
)

JAVA_PROGRAM(ydb-url-shortener-demo)

JDK_VERSION(11)
JAVAC_FLAGS(--release 8)

OWNER(g:kikimr)

JAVA_SRCS(SRCDIR src/main/java **/*)
JAVA_SRCS(SRCDIR src/main/resources **/*)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/examples/ya.dependencies.inc)

PEERDIR(
    kikimr/public/sdk/java/table

    contrib/java/org/apache/logging/log4j/log4j-slf4j-impl
    contrib/java/com/beust/jcommander

    contrib/java/org/eclipse/jetty/jetty-server
    contrib/java/org/eclipse/jetty/jetty-servlet
)

LINT(extended)
END()

