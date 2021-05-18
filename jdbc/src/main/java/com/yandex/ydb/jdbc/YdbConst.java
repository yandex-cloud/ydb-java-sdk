package com.yandex.ydb.jdbc;

import java.sql.Connection;

public class YdbConst {
    public static final String YDB_DRIVER_USES_SL4J = "YDB Driver uses SLF4j";

    public static final String PREPARED_CALLS_UNSUPPORTED = "Prepared calls are not supported";
    public static final String ARRAYS_UNSUPPORTED = "Arrays are not supported";
    public static final String STRUCTS_UNSUPPORTED = "Structs are not supported";
    public static final String BLOB_UNSUPPORTED = "Blobs are not supported";
    public static final String NCLOB_UNSUPPORTED = "NClobs are not supported";
    public static final String CLOB_UNSUPPORTED = "Clobs are not supported";
    public static final String SQLXML_UNSUPPORTED = "SQLXMLs are not supported";
    public static final String SAVEPOINTS_UNSUPPORTED = "Savepoints are not supported";
    public static final String AUTO_GENERATED_KEYS_UNSUPPORTED = "Auto-generated keys are not supported";
    public static final String CURSOR_UPDATING_UNSUPPORTED = "Cursor updates are not supported";
    public static final String ROWID_UNSUPPORTED = "RowIds are not supported";
    public static final String NAMED_CURSORS_UNSUPPORTED = "Named cursors are not supported";
    public static final String REF_UNSUPPORTED = "Refs are not supported";
    public static final String ASCII_STREAM_UNSUPPORTED = "AsciiStreams are not supported";

    public static final String FORWARD_ONLY_MODE = "ResultSet in TYPE_FORWARD_ONLY mode";
    public static final String CUSTOM_SQL_UNSUPPORTED = "PreparedStatement cannot execute custom SQL";
    public static final String ABORT_UNSUPPORTED = "Abort operation is not supported yet";
    public static final String SET_NETWORK_TIMEOUT_UNSUPPORTED = "Set network timeout is not supported yet";
    public static final String OBJECT_TYPED_UNSUPPORTED = "Object with type conversion is not supported yet";
    public static final String QUERY_EXPECT_RESULT_SET = "Query must return ResultSet";
    public static final String QUERY_EXPECT_UPDATE = "Query must not return ResultSet";
    public static final String UNABLE_TO_SET_NULL_VALUE = "Unable to set NULL value, param is mandatory: ";
    public static final String DIRECTION_UNSUPPORTED = "Direction is not supported: ";
    public static final String RESULT_SET_MODE_UNSUPPORTED = "ResultSet mode is not supported: ";
    public static final String RESULT_SET_UNAVAILABLE = "ResultSet is not available at index: ";
    public static final String RESULT_IS_TRUNCATED = "Result #%s was truncated to %s rows";
    public static final String INVALID_FETCH_DIRECTION = "Fetch direction %s cannot be used when result set type is %s";
    public static final String COLUMN_NOT_FOUND = "Column not found: ";
    public static final String COLUMN_NUMBER_NOT_FOUND = "Column is out of range: ";
    public static final String PARAMETER_NUMBER_NOT_FOUND = "Parameter is out of range: ";
    public static final String PARAMETER_NOT_FOUND = "Parameter not found: ";
    public static final String INVALID_ROW = "Current row index is out of bounds: ";
    public static final String BATCH_UNSUPPORTED = "Batches are not supported in simple prepared statements";
    public static final String METADATA_RS_UNSUPPORTED_IN_PS = "ResultSet metadata is not supported " +
            "in prepared statements";

    public static final String INDEXED_PARAMETERS_UNSUPPORTED = "Indexed parameters are not supported in YDB";

    public static final String NOTHING_TO_UNWRAP = "Nothing to unwrap";

    /**
     * All transactions are serialized one-by-one. If the DB detects a write collision among
     * several concurrent transactions, only the first one is committed.
     * This is the strongest level. And the only level at which data changes are possible.
     */
    public static final int TRANSACTION_SERIALIZABLE_READ_WRITE = Connection.TRANSACTION_SERIALIZABLE;
    /**
     * The most recent consistent state of the database. Read only.
     */
    public static final int ONLINE_CONSISTENT_READ_ONLY = Connection.TRANSACTION_REPEATABLE_READ;
    /**
     * The most recent inconsistent state of the database. Read only.
     * A phantom read may occurs when, in the course of a transaction, some new rows are added
     * by another transaction to the records being read. This is the weakest level.
     */
    public static final int ONLINE_INCONSISTENT_READ_ONLY = Connection.TRANSACTION_READ_COMMITTED;
    /**
     * An <em>almost</em> recent consistent state of the database. Read only.
     * This level is faster then {@code ONLINE_CONSISTENT_READ_ONLY}, but may return stale data.
     */
    public static final int STALE_CONSISTENT_READ_ONLY = 3; // TODO: verify if we can do that
    public static final String RESULT_SET_TYPE_UNSUPPORTED =
            "resultSetType must be ResultSet.TYPE_FORWARD_ONLY or ResultSet.TYPE_SCROLL_INSENSITIVE";
    public static final String RESULT_SET_CONCURRENCY_UNSUPPORTED =
            "resultSetConcurrency must be ResultSet.CONCUR_READ_ONLY";
    public static final String RESULT_SET_HOLDABILITY_UNSUPPORTED =
            "resultSetHoldability must be ResultSet.HOLD_CURSORS_OVER_COMMIT";
    public static final String READONLY_INSIDE_TRANSACTION = "Cannot change read-only attribute inside a transaction";
    public static final String CHANGE_ISOLATION_INSIDE_TX = "Cannot change transaction isolation inside a transaction";
    public static final String UNSUPPORTED_TRANSACTION_LEVEL = "Unsupported transaction level: ";
    public static final String CLOSED_CONNECTION = "Connection is closed";
    public static final String DB_QUERY_DEADLINE_EXCEEDED = "DB query deadline exceeded: ";
    public static final String DB_QUERY_CANCELLED = "DB query cancelled: ";
    public static final String DATABASE_QUERY_INTERRUPTED = "Database query interrupted";
    public static final String DATABASE_UNAVAILABLE = "Database is unavailable: ";
    public static final String CANNOT_LOAD_DATA_FROM_IS = "Unable to load data from input stream: ";
    public static final String CANNOT_LOAD_DATA_FROM_READER = "Unable to load data from reader: ";
    public static final String UNSUPPORTED_QUERY_TYPE_IN_PS = "Query type in prepared statement not supported: ";


    // "Cannot cast" is used in tests for checking errors
    public static final String UNABLE_TO_CAST = "Cannot cast [%s] to [%s]";
    public static final String UNABLE_TO_CONVERT = "Cannot cast [%s] with value [%s] to [%s]";
    public static final String UNABLE_TO_CONVERT_AS_URL = "Cannot cast as URL: ";

    public static final String MISSING_VALUE_FOR_PARAMETER = "Missing value for parameter: ";
    public static final String MISSING_REQUIRED_VALUE = "Missing required value for parameter: ";
    public static final String INVALID_PARAMETER_TYPE = "Cannot cast parameter [%s] from [%s] to [%s]";

    public static final String PREFIX_SYNTAX_V1 = "--!syntax_v1";


    public static final String EXPLAIN_COLUMN_AST = "AST";
    public static final String EXPLAIN_COLUMN_PLAN = "PLAN";

    private YdbConst() {
        //
    }
}
