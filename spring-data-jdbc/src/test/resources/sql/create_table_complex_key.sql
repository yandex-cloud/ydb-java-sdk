--jdbc:SCHEME

create table ${tableName}
(
    intKey    Int32,
    stringKey Utf8,

    value     Utf8,

    primary key (intKey, stringKey)
)
