SET(grpc_version 1.26.0) # sync version with ydb/public/api/grpc
SET(yandex_cloud_version 2.0.0)
SET(slf4j_version 1.7.32)

DEPENDENCY_MANAGEMENT(
    # GRPC
    contrib/java/io/grpc/grpc-netty/${grpc_version}

    # Yandex Cloud
    contrib/java/com/yandex/cloud/java-sdk-auth/${yandex_cloud_version}

    # slf4j
    contrib/java/org/slf4j/slf4j-api/${slf4j_version}
)

