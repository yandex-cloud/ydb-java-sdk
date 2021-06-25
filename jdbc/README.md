[Experimental] JDBC Driver for YDB
---
This is an experimental version of JDBC driver for YDB. It is in active development and is not intended for use in production environments.

## Quickstart

1) Drop in [JDBC driver](https://proxy.sandbox.yandex-team.ru/2245355765) to classpath or include to your ya.make file
2) Connect to YDB
   * Local or remote Docker: `jdbc:ydb:localhost:2135/local` or `jdbc:ydb:localhost:2135?database=/local`
   * Dedicated: `jdbc:ydb:ydb-ru-prestable.yandex.net:2135?database=/ru-prestable/home/miroslav2/mydb&token=~/.arc/token` where `/ru-prestable/home/miroslav2/mydb` is your database and `~/.arc/token` is a location of your YDB token file.

![Screen with IDEA settings](https://jing.yandex-team.ru/files/miroslav2/Screenshot%202021-06-25%20at%2016.42.29.png "IDEA settings example")
