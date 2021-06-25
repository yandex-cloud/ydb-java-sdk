--jdbc:SCHEME

create table ${tableName}
(
    intKey    Int32,
    stringKey Utf8,

    value1    Utf8,
    value2    String,

    primary key (intKey, stringKey)
)
