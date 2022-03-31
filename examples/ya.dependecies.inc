SET(log4j_version 2.17.1)
SET(slf4j_version 1.7.28)
SET(spring_boot_version 2.0.3.RELEASE)
SET(junit5_version 5.8.2)
SET(jcommander_version 1.81)
SET(jetty_version 9.4.45.v20220203)
SET(testcontainers_version 1.15.2)

DEPENDENCY_MANAGEMENT(
    # spring boot
    contrib/java/org/springframework/boot/spring-boot/${spring_boot_version}
    contrib/java/org/springframework/boot/spring-boot-starter-web/${spring_boot_version}

    # slf4j
    contrib/java/org/slf4j/slf4j-api/${slf4j_version}
    contrib/java/org/slf4j/slf4j-simple/${slf4j_version}

    # log4j
    contrib/java/org/apache/logging/log4j/log4j-api/${log4j_version}
    contrib/java/org/apache/logging/log4j/log4j-core/${log4j_version}
    contrib/java/org/apache/logging/log4j/log4j-slf4j-impl/${log4j_version}

    # junit
    contrib/java/org/junit/jupiter/junit-jupiter/${junit5_version}
    contrib/java/org/junit/jupiter/junit-jupiter-api/${junit5_version}
    contrib/java/org/junit/vintage/junit-vintage-engine/${junit5_version}

    # JCommander
    contrib/java/com/beust/jcommander/${jcommander_version}

    # Jetty
    contrib/java/org/eclipse/jetty/jetty-server/${jetty_version}
    contrib/java/org/eclipse/jetty/jetty-servlet/${jetty_version}

    # TestContainers
    contrib/java/org/testcontainers/testcontainers/${testcontainers_version}
)

