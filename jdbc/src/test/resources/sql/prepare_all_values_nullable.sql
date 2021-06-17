declare $key as Int32?;

declare $c_Bool as Bool?;
declare $c_Int32 as Int32?;
declare $c_Int64 as Int64?;
declare $c_Uint8 as Uint8?;
declare $c_Uint32 as Uint32?;
declare $c_Uint64 as Uint64?;
declare $c_Float as Float?;
declare $c_Double as Double?;
declare $c_String as String?;
declare $c_Utf8 as Utf8?;
declare $c_Json as Json?;
declare $c_JsonDocument as JsonDocument?;
declare $c_Yson as Yson?;
declare $c_Date as Date?;
declare $c_Datetime as Datetime?;
declare $c_Timestamp as Timestamp?;
declare $c_Interval as Interval?;
declare $c_Decimal as Decimal(22,9)?;

upsert into ${tableName} (
    key,
    c_Bool,
    c_Int32,
    c_Int64,
    c_Uint8,
    c_Uint32,
    c_Uint64,
    c_Float,
    c_Double,
    c_String,
    c_Utf8,
    c_Json,
    c_JsonDocument,
    c_Yson,
    c_Date,
    c_Datetime,
    c_Timestamp,
    c_Interval,
    c_Decimal
) values (
    $key,
    $c_Bool,
    $c_Int32,
    $c_Int64,
    $c_Uint8,
    $c_Uint32,
    $c_Uint64,
    $c_Float,
    $c_Double,
    $c_String,
    $c_Utf8,
    $c_Json,
    $c_JsonDocument,
    $c_Yson,
    $c_Date,
    $c_Datetime,
    $c_Timestamp,
    $c_Interval,
    $c_Decimal
)
