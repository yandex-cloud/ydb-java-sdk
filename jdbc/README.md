[Experimental] JDBC Driver for YDB
---
This is an experimental version of JDBC driver for YDB. It is in active development and is not intended for use in production environments.

## Quickstart

1) Drop in [JDBC driver](https://proxy.sandbox.yandex-team.ru/2248455883) to classpath or include to your ya.make file
2) Connect to YDB
   * Local or remote Docker: `jdbc:ydb:localhost:2135/local` or `jdbc:ydb:localhost:2135?database=/local`
   * Dedicated: `jdbc:ydb:ydb-ru-prestable.yandex.net:2135?database=/ru-prestable/home/miroslav2/mydb&token=~/.arc/token` where `/ru-prestable/home/miroslav2/mydb` is your database and `~/.arc/token` is a location of your YDB token file.
3) Execute queries, see example in [YdbDriverExampleTest.java](src/test/java/com/yandex/ydb/jdbc/YdbDriverExampleTest.java)
