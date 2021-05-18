--jdbc:SCHEME

create table ${tableName}
(
    key            Int32,
    c_Bool         Bool,
    -- unsupported, c_Int8        Int8,
    -- unsupported, c_Int16        Int16,
    c_Int32        Int32,
    c_Int64        Int64,
    c_Uint8        Uint8,
    -- unsupported, c_Uint16        Uint16,
    c_Uint32       Uint32,
    c_Uint64       Uint64,
    c_Float        Float,
    c_Double       Double,
    c_String       String,
    c_Utf8         Utf8,
    c_Json         Json,
    c_JsonDocument JsonDocument,
    c_Yson         Yson,
    c_Date         Date,
    c_Datetime     Datetime,
    c_Timestamp    Timestamp,
    c_Interval     Interval,
    c_Decimal      Decimal(22,9),
    -- unsupported c_Uuid Uuid,
    -- unsupported c_TzDate TzDate,
    -- unsupported c_TzDatetime TzDatetime,
    -- unsupported c_TzTimestamp TzTimestamp,
    -- unsupported c_List List,
    -- unsupported c_Struct Struct,
    -- unsupported c_Tuple Tuple,
    -- unsupported c_Dict Dict,
    -- TODO: support DyNumber?
    primary key (key)
)
