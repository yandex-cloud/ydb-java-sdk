[Experimental] JDBC Driver for YDB
---
This is an experimental version of JDBC driver for YDB. It is in active development and is not intended for use in production environments.

## Building
All tests are run without Docker by default.
To enable all tests make sure you have Docker or Docker Machine installed then run `mvn install -DSKIP_DOCKER_TESTS=false`

## Quickstart

1) Drop in [JDBC driver](https://repo1.maven.org/maven2/com/yandex/ydb/ydb-sdk-jdbc-uberjar/1.8.0/ydb-sdk-jdbc-uberjar-1.8.0.jar) to classpath or pick this file in IDEA
2) Connect to YDB
   * Local or remote Docker: `jdbc:ydb:localhost:2135/local` or `jdbc:ydb:localhost:2135?database=/local`
   * Dedicated: `jdbc:ydb:ydb-ru-prestable.yandex.net:2135?database=/ru-prestable/home/miroslav2/mydb&token=~/.arc/token` where `/ru-prestable/home/miroslav2/mydb` is your database and `~/.arc/token` is a location of your YDB token file.
3) Execute queries, see example in [YdbDriverExampleTest.java](src/test/java/com/yandex/ydb/jdbc/YdbDriverExampleTest.java)
