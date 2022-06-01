SET(log4j_version 2.17.2)
SET(google_truth_version 0.34)
SET(junit_version 4.13.2)
SET(mockito_version 3.11.2)
SET(mock_server_version 5.5.1)
SET(testcontainers_version 1.15.2)

DEPENDENCY_MANAGEMENT(
    # log4j
    contrib/java/org/apache/logging/log4j/log4j-api/${log4j_version}
    contrib/java/org/apache/logging/log4j/log4j-core/${log4j_version}
    contrib/java/org/apache/logging/log4j/log4j-slf4j-impl/${log4j_version}

    # JUnit 4
    contrib/java/junit/junit/${junit_version}

    # Mockito
    contrib/java/org/mockito/mockito-core/${mockito_version}
    contrib/java/org/mockito/mockito-inline/${mockito_version}

    # Mock Server
    contrib/java/org/mock-server/mockserver-netty/${mock_server_version}

    # Google Truth
    contrib/java/com/google/truth/truth/${google_truth_version}
    contrib/java/com/google/truth/extensions/truth-proto-extension/${google_truth_version}

    # TestContainers
    contrib/java/org/testcontainers/testcontainers/${testcontainers_version}
)

