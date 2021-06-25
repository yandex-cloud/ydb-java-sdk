--jdbc:SCHEME

create table ${tableName}
(
    id                  Int32,
    customColumn        Utf8,


    booleanValue        Bool,
    booleanValueOpt     Bool,

    byteValue           Int32,
    byteValueOpt        Int32,

    shortValue          Int32,
    shortValueOpt       Int32,

    intValue            Int32,
    intValueOpt         Int32,

    uintValue           Int32,

    longValue           Int64,
    longValueOpt        Int64,

    floatValue          Float,
    floatValueOpt       Float,

    doubleValue         Double,
    doubleValueOpt      Double,

    stringValue         Utf8,
    stringValueAsString String,

    bigIntegerValue     Int64,
    bigDecimalValue     Decimal(22, 9),

    bytesValue          String,

    dateValue           Timestamp,
    localDateValue      Date,
    localDateTimeValue  DateTime,

    enumValue           Utf8,
    instantValue        Timestamp,
    decimalValue        Decimal(22, 9),

    primary key (id),
    INDEX INDEX_STRING_VALUE GLOBAL ON (stringValue)
)
