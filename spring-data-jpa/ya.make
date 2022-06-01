JAVA_LIBRARY(ydb-spring-data-jpa)

IF(JDK_VERSION == "")
    JDK_VERSION(11)
ENDIF()
MAVEN_GROUP_ID(com.yandex.ydb)

OWNER(g:kikimr)

JAVA_SRCS(
    SRCDIR src/main/java
    **/*.java
)

PEERDIR(
    kikimr/public/sdk/java/jdbc
    contrib/java/org/springframework/data/spring-data-jpa/2.5.2
    contrib/java/org/hibernate/hibernate-entitymanager/5.4.32.Final
)

LINT(base)
END()

RECURSE_FOR_TESTS(
    ut
)
