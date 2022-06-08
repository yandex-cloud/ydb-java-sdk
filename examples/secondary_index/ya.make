JAVA_PROGRAM(ydb-examples-secondary-index)

JDK_VERSION(11)
JAVAC_FLAGS(--release 8)

OWNER(g:kikimr)

JAVA_SRCS(SRCDIR src/main/java **/*)
JAVA_SRCS(SRCDIR src/main/resources **/*)

INCLUDE(${ARCADIA_ROOT}/kikimr/public/sdk/java/examples/ya.dependencies.inc)

PEERDIR(
    contrib/java/org/springframework/boot/spring-boot-starter-web
    contrib/java/javax/validation/validation-api
    
    kikimr/public/sdk/java/table
)

LINT(extended)
END()
