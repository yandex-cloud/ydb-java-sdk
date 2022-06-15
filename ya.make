OWNER(g:kikimr)

RECURSE(
    auth-iam
    auth-iam/ut
    core
    core/ut
    examples
    table
    table/ut
    jdbc
    spring-data-jdbc
    spring-data-jpa
)

IF (NOT HARDENING AND NOT SANITIZER_TYPE)
    RECURSE(
        auth-tvm
        auth-tvm/ut
    )
ENDIF()
