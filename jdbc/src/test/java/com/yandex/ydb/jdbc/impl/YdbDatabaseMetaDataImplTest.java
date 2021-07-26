package com.yandex.ydb.jdbc.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yandex.ydb.jdbc.TestHelper;
import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbConst;
import com.yandex.ydb.jdbc.YdbDatabaseMetaData;
import com.yandex.ydb.jdbc.YdbDriverInfo;
import com.yandex.ydb.jdbc.YdbStatement;
import com.yandex.ydb.jdbc.exception.YdbConfigurationException;
import com.yandex.ydb.jdbc.settings.YdbProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yandex.ydb.jdbc.TestHelper.TEST_TYPE;
import static com.yandex.ydb.jdbc.TestHelper.UNIVERSAL;
import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsg;
import static com.yandex.ydb.jdbc.impl.MappingResultSets.stableMap;
import static com.yandex.ydb.jdbc.impl.YdbDatabaseMetaDataImpl.SYSTEM_TABLE;
import static com.yandex.ydb.jdbc.impl.YdbDatabaseMetaDataImpl.TABLE;
import static java.sql.DatabaseMetaData.bestRowSession;
import static java.sql.DatabaseMetaData.sqlStateSQL;
import static java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT;
import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static java.sql.ResultSet.TYPE_SCROLL_SENSITIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIfSystemProperty(named = TEST_TYPE, matches = UNIVERSAL)
class YdbDatabaseMetaDataImplTest extends AbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(YdbDatabaseMetaDataImplTest.class);

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    private YdbConnection connection;
    private YdbDatabaseMetaData metaData;

    @BeforeEach
    void beforeEach() throws SQLException {
        connection = getTestConnection();
        metaData = connection.getMetaData();
        configureOnce(AbstractTest::recreateSimpleTestTable);
    }

    @Test
    void allProceduresAreCallable() throws SQLException {
        assertFalse(metaData.allProceduresAreCallable());
    }

    @Test
    void allTablesAreSelectable() throws SQLException {
        assertTrue(metaData.allTablesAreSelectable());
    }

    @Test
    void getURL() throws SQLException {
        String url = metaData.getURL();
        assertNotNull(url);
        assertEquals(TestHelper.getTestUrl(), url);
    }

    @Test
    void getUserName() throws SQLException {
        assertEquals("", metaData.getUserName());
    }

    @Test
    void isReadOnly() throws SQLException {
        assertFalse(metaData.isReadOnly());
        connection.setTransactionIsolation(YdbConst.ONLINE_CONSISTENT_READ_ONLY);
        assertTrue(metaData.isReadOnly());
    }

    @Test
    void nullsAreSortedHigh() throws SQLException {
        assertTrue(metaData.nullsAreSortedHigh());
    }

    @Test
    void nullsAreSortedLow() throws SQLException {
        assertFalse(metaData.nullsAreSortedLow());
    }

    @Test
    void nullsAreSortedAtStart() throws SQLException {
        assertFalse(metaData.nullsAreSortedAtStart());
    }

    @Test
    void nullsAreSortedAtEnd() throws SQLException {
        assertFalse(metaData.nullsAreSortedAtEnd());
    }

    @Test
    void getDatabaseProductName() throws SQLException {
        assertEquals("YDB", metaData.getDatabaseProductName());
    }

    @Test
    void getDatabaseProductVersion() throws SQLException {
        assertEquals("unspecified", metaData.getDatabaseProductVersion());
    }

    @Test
    void getDriverName() throws SQLException {
        assertEquals(YdbDriverInfo.DRIVER_NAME, metaData.getDriverName());
    }

    @Test
    void getDriverVersion() throws SQLException {
        assertEquals(YdbDriverInfo.DRIVER_VERSION, metaData.getDriverVersion());
        if (YdbDriverInfo.DRIVER_MINOR_VERSION == 0) {
            assertEquals(YdbVersionCollector.LATEST_VERSION, metaData.getDriverVersion());
        } else {
            assertNotEquals(YdbVersionCollector.LATEST_VERSION, metaData.getDriverVersion());
        }
    }

    @Test
    void getDriverMajorVersion() {
        assertEquals(YdbDriverInfo.DRIVER_MAJOR_VERSION, metaData.getDriverMajorVersion());
        assertEquals(1, metaData.getDriverMajorVersion());
    }

    @Test
    void getDriverMinorVersion() {
        assertEquals(YdbDriverInfo.DRIVER_MINOR_VERSION, metaData.getDriverMinorVersion());
        assertTrue(metaData.getDriverMajorVersion() >= 0);
    }

    @Test
    void usesLocalFiles() throws SQLException {
        assertFalse(metaData.usesLocalFiles());
    }

    @Test
    void usesLocalFilePerTable() throws SQLException {
        assertFalse(metaData.usesLocalFilePerTable());
    }

    @Test
    void supportsMixedCaseIdentifiers() throws SQLException {
        assertTrue(metaData.supportsMixedCaseIdentifiers());
    }

    @Test
    void storesUpperCaseIdentifiers() throws SQLException {
        assertFalse(metaData.storesUpperCaseIdentifiers());
    }

    @Test
    void storesLowerCaseIdentifiers() throws SQLException {
        assertFalse(metaData.storesLowerCaseIdentifiers());
    }

    @Test
    void storesMixedCaseIdentifiers() throws SQLException {
        assertTrue(metaData.storesMixedCaseIdentifiers());
    }

    @Test
    void supportsMixedCaseQuotedIdentifiers() throws SQLException {
        assertTrue(metaData.supportsMixedCaseQuotedIdentifiers());
    }

    @Test
    void storesUpperCaseQuotedIdentifiers() throws SQLException {
        assertFalse(metaData.storesUpperCaseQuotedIdentifiers());
    }

    @Test
    void storesLowerCaseQuotedIdentifiers() throws SQLException {
        assertFalse(metaData.storesLowerCaseQuotedIdentifiers());
    }

    @Test
    void storesMixedCaseQuotedIdentifiers() throws SQLException {
        assertTrue(metaData.storesMixedCaseQuotedIdentifiers());
    }

    @Test
    void getIdentifierQuoteString() throws SQLException {
        assertEquals("`", metaData.getIdentifierQuoteString());
    }

    @Test
    void getSQLKeywords() throws SQLException {
        assertEquals("", metaData.getSQLKeywords());
    }

    @Test
    void getNumericFunctions() throws SQLException {
        LOGGER.info("getNumericFunctions: {}", metaData.getNumericFunctions());
        assertSame(metaData.getNumericFunctions(), metaData.getNumericFunctions());
    }

    @Test
    void getStringFunctions() throws SQLException {
        LOGGER.info("getNumericFunctions: {}", metaData.getStringFunctions());
        assertSame(metaData.getStringFunctions(), metaData.getStringFunctions());

        assertNotEquals(metaData.getNumericFunctions(), metaData.getStringFunctions());
    }

    @Test
    void getSystemFunctions() throws SQLException {
        LOGGER.info("getSystemFunctions: {}", metaData.getSystemFunctions());
        assertSame(metaData.getSystemFunctions(), metaData.getSystemFunctions());

        assertNotEquals(metaData.getNumericFunctions(), metaData.getSystemFunctions());
        assertNotEquals(metaData.getStringFunctions(), metaData.getSystemFunctions());
    }

    @Test
    void getTimeDateFunctions() throws SQLException {
        LOGGER.info("getTimeDateFunctions: {}", metaData.getTimeDateFunctions());
        assertSame(metaData.getTimeDateFunctions(), metaData.getTimeDateFunctions());

        assertNotEquals(metaData.getNumericFunctions(), metaData.getTimeDateFunctions());
        assertNotEquals(metaData.getStringFunctions(), metaData.getTimeDateFunctions());
        assertNotEquals(metaData.getSystemFunctions(), metaData.getTimeDateFunctions());
    }

    @Test
    void getSearchStringEscape() throws SQLException {
        assertEquals("\\", metaData.getSearchStringEscape());
    }

    @Test
    void getExtraNameCharacters() throws SQLException {
        assertEquals("", metaData.getExtraNameCharacters());
    }

    @Test
    void supportsAlterTableWithAddColumn() throws SQLException {
        assertTrue(metaData.supportsAlterTableWithAddColumn());
    }

    @Test
    void supportsAlterTableWithDropColumn() throws SQLException {
        assertTrue(metaData.supportsAlterTableWithDropColumn());
    }

    @Test
    void supportsColumnAliasing() throws SQLException {
        assertTrue(metaData.supportsColumnAliasing());
    }

    @Test
    void nullPlusNonNullIsNull() throws SQLException {
        assertTrue(metaData.nullPlusNonNullIsNull());
    }

    @Test
    void supportsConvert() throws SQLException {
        assertFalse(metaData.supportsConvert());
        assertFalse(metaData.supportsConvert(Types.INTEGER, Types.BIGINT));
    }

    @Test
    void supportsTableCorrelationNames() throws SQLException {
        assertTrue(metaData.supportsTableCorrelationNames());
    }

    @Test
    void supportsDifferentTableCorrelationNames() throws SQLException {
        assertFalse(metaData.supportsDifferentTableCorrelationNames());
    }

    @Test
    void supportsExpressionsInOrderBy() throws SQLException {
        assertTrue(metaData.supportsExpressionsInOrderBy());
    }

    @Test
    void supportsOrderByUnrelated() throws SQLException {
        assertFalse(metaData.supportsOrderByUnrelated());
    }

    @Test
    void supportsGroupBy() throws SQLException {
        assertTrue(metaData.supportsGroupBy());
    }

    @Test
    void supportsGroupByUnrelated() throws SQLException {
        assertTrue(metaData.supportsGroupByUnrelated());
    }

    @Test
    void supportsGroupByBeyondSelect() throws SQLException {
        assertTrue(metaData.supportsGroupByBeyondSelect());
    }

    @Test
    void supportsLikeEscapeClause() throws SQLException {
        assertTrue(metaData.supportsLikeEscapeClause());
    }

    @Test
    void supportsMultipleResultSets() throws SQLException {
        assertTrue(metaData.supportsMultipleResultSets());
    }

    @Test
    void supportsMultipleTransactions() throws SQLException {
        assertTrue(metaData.supportsMultipleTransactions());
    }

    @Test
    void supportsNonNullableColumns() throws SQLException {
        assertFalse(metaData.supportsNonNullableColumns());
    }

    @Test
    void supportsMinimumSQLGrammar() throws SQLException {
        assertTrue(metaData.supportsMinimumSQLGrammar());
    }

    @Test
    void supportsCoreSQLGrammar() throws SQLException {
        assertFalse(metaData.supportsCoreSQLGrammar());
    }

    @Test
    void supportsExtendedSQLGrammar() throws SQLException {
        assertFalse(metaData.supportsExtendedSQLGrammar());
    }

    @Test
    void supportsANSI92EntryLevelSQL() throws SQLException {
        assertFalse(metaData.supportsANSI92EntryLevelSQL());
    }

    @Test
    void supportsANSI92IntermediateSQL() throws SQLException {
        assertFalse(metaData.supportsANSI92IntermediateSQL());
    }

    @Test
    void supportsANSI92FullSQL() throws SQLException {
        assertFalse(metaData.supportsANSI92FullSQL());
    }

    @Test
    void supportsIntegrityEnhancementFacility() throws SQLException {
        assertFalse(metaData.supportsIntegrityEnhancementFacility());
    }

    @Test
    void supportsOuterJoins() throws SQLException {
        assertTrue(metaData.supportsOuterJoins());
    }

    @Test
    void supportsFullOuterJoins() throws SQLException {
        assertTrue(metaData.supportsFullOuterJoins());
    }

    @Test
    void supportsLimitedOuterJoins() throws SQLException {
        assertTrue(metaData.supportsLimitedOuterJoins());
    }

    @Test
    void getSchemaTerm() throws SQLException {
        assertEquals("Database", metaData.getSchemaTerm());
    }

    @Test
    void getProcedureTerm() throws SQLException {
        assertEquals("", metaData.getProcedureTerm());
    }

    @Test
    void getCatalogTerm() throws SQLException {
        assertEquals("Path", metaData.getCatalogTerm());
    }

    @Test
    void isCatalogAtStart() throws SQLException {
        assertTrue(metaData.isCatalogAtStart());
    }

    @Test
    void getCatalogSeparator() throws SQLException {
        assertEquals("/", metaData.getCatalogSeparator());
    }

    @Test
    void supportsSchemasInDataManipulation() throws SQLException {
        assertFalse(metaData.supportsSchemasInDataManipulation());
    }

    @Test
    void supportsSchemasInProcedureCalls() throws SQLException {
        assertFalse(metaData.supportsSchemasInProcedureCalls());
    }

    @Test
    void supportsSchemasInTableDefinitions() throws SQLException {
        assertFalse(metaData.supportsSchemasInTableDefinitions());
    }

    @Test
    void supportsSchemasInIndexDefinitions() throws SQLException {
        assertFalse(metaData.supportsSchemasInIndexDefinitions());
    }

    @Test
    void supportsSchemasInPrivilegeDefinitions() throws SQLException {
        assertFalse(metaData.supportsSchemasInIndexDefinitions());
    }

    @Test
    void supportsCatalogsInDataManipulation() throws SQLException {
        assertTrue(metaData.supportsCatalogsInDataManipulation());
    }

    @Test
    void supportsCatalogsInProcedureCalls() throws SQLException {
        assertTrue(metaData.supportsCatalogsInProcedureCalls());
    }

    @Test
    void supportsCatalogsInTableDefinitions() throws SQLException {
        assertTrue(metaData.supportsCatalogsInTableDefinitions());
    }

    @Test
    void supportsCatalogsInIndexDefinitions() throws SQLException {
        assertTrue(metaData.supportsCatalogsInIndexDefinitions());
    }

    @Test
    void supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        assertTrue(metaData.supportsCatalogsInPrivilegeDefinitions());
    }

    @Test
    void supportsPositionedDelete() throws SQLException {
        assertFalse(metaData.supportsPositionedDelete());
    }

    @Test
    void supportsPositionedUpdate() throws SQLException {
        assertFalse(metaData.supportsPositionedUpdate());
    }

    @Test
    void supportsSelectForUpdate() throws SQLException {
        assertFalse(metaData.supportsSelectForUpdate());
    }

    @Test
    void supportsStoredProcedures() throws SQLException {
        assertFalse(metaData.supportsStoredProcedures());
    }

    @Test
    void supportsSubqueriesInComparisons() throws SQLException {
        assertTrue(metaData.supportsSubqueriesInComparisons());
    }

    @Test
    void supportsSubqueriesInExists() throws SQLException {
        assertTrue(metaData.supportsSubqueriesInExists());
    }

    @Test
    void supportsSubqueriesInIns() throws SQLException {
        assertTrue(metaData.supportsSubqueriesInIns());
    }

    @Test
    void supportsSubqueriesInQuantifieds() throws SQLException {
        assertTrue(metaData.supportsSubqueriesInQuantifieds());
    }

    @Test
    void supportsCorrelatedSubqueries() throws SQLException {
        assertTrue(metaData.supportsCorrelatedSubqueries());
    }

    @Test
    void supportsUnion() throws SQLException {
        assertFalse(metaData.supportsUnion());
    }

    @Test
    void supportsUnionAll() throws SQLException {
        assertTrue(metaData.supportsUnionAll());
    }

    @Test
    void supportsOpenCursorsAcrossCommit() throws SQLException {
        assertTrue(metaData.supportsOpenCursorsAcrossCommit());
    }

    @Test
    void supportsOpenCursorsAcrossRollback() throws SQLException {
        assertTrue(metaData.supportsOpenCursorsAcrossRollback());
    }

    @Test
    void supportsOpenStatementsAcrossCommit() throws SQLException {
        assertTrue(metaData.supportsOpenStatementsAcrossCommit());
    }

    @Test
    void supportsOpenStatementsAcrossRollback() throws SQLException {
        assertTrue(metaData.supportsOpenStatementsAcrossRollback());
    }

    @Test
    void getMaxBinaryLiteralLength() throws SQLException {
        assertEquals(YdbConst.MAX_COLUMN_SIZE, metaData.getMaxBinaryLiteralLength());
    }

    @Test
    void getMaxCharLiteralLength() throws SQLException {
        assertEquals(YdbConst.MAX_COLUMN_SIZE, metaData.getMaxCharLiteralLength());
    }

    @Test
    void getMaxColumnNameLength() throws SQLException {
        assertEquals(YdbConst.MAX_COLUMN_NAME_LENGTH, metaData.getMaxColumnNameLength());
    }

    @Test
    void getMaxColumnsInGroupBy() throws SQLException {
        assertEquals(YdbConst.MAX_COLUMNS, metaData.getMaxColumnsInGroupBy());
    }

    @Test
    void getMaxColumnsInIndex() throws SQLException {
        assertEquals(YdbConst.MAX_COLUMNS_IN_PRIMARY_KEY, metaData.getMaxColumnsInIndex());
    }

    @Test
    void getMaxColumnsInOrderBy() throws SQLException {
        assertEquals(YdbConst.MAX_COLUMNS, metaData.getMaxColumnsInOrderBy());
    }

    @Test
    void getMaxColumnsInSelect() throws SQLException {
        assertEquals(YdbConst.MAX_COLUMNS, metaData.getMaxColumnsInSelect());
    }

    @Test
    void getMaxColumnsInTable() throws SQLException {
        assertEquals(YdbConst.MAX_COLUMNS, metaData.getMaxColumnsInTable());
    }

    @Test
    void getMaxConnections() throws SQLException {
        assertEquals(YdbConst.MAX_CONNECTIONS, metaData.getMaxConnections());
    }

    @Test
    void getMaxCursorNameLength() throws SQLException {
        assertEquals(YdbConst.MAX_ELEMENT_NAME_LENGTH, metaData.getMaxCursorNameLength());
    }

    @Test
    void getMaxIndexLength() throws SQLException {
        assertEquals(YdbConst.MAX_PRIMARY_KEY_SIZE, metaData.getMaxIndexLength());
    }

    @Test
    void getMaxSchemaNameLength() throws SQLException {
        assertEquals(YdbConst.MAX_ELEMENT_NAME_LENGTH, metaData.getMaxSchemaNameLength());
    }

    @Test
    void getMaxProcedureNameLength() throws SQLException {
        assertEquals(YdbConst.MAX_ELEMENT_NAME_LENGTH, metaData.getMaxProcedureNameLength());
    }

    @Test
    void getMaxCatalogNameLength() throws SQLException {
        assertEquals(YdbConst.MAX_ELEMENT_NAME_LENGTH, metaData.getMaxCatalogNameLength());
    }

    @Test
    void getMaxRowSize() throws SQLException {
        assertEquals(YdbConst.MAX_ROW_SIZE, metaData.getMaxRowSize());
    }

    @Test
    void doesMaxRowSizeIncludeBlobs() throws SQLException {
        assertTrue(metaData.doesMaxRowSizeIncludeBlobs());
    }

    @Test
    void getMaxStatementLength() throws SQLException {
        assertEquals(YdbConst.MAX_STATEMENT_LENGTH, metaData.getMaxStatementLength());
    }

    @Test
    void getMaxStatements() throws SQLException {
        assertEquals(0, metaData.getMaxStatements());
    }

    @Test
    void getMaxTableNameLength() throws SQLException {
        assertEquals(YdbConst.MAX_ELEMENT_NAME_LENGTH, metaData.getMaxTableNameLength());
    }

    @Test
    void getMaxTablesInSelect() throws SQLException {
        assertEquals(0, metaData.getMaxStatements());
    }

    @Test
    void getMaxUserNameLength() throws SQLException {
        assertEquals(YdbConst.MAX_ELEMENT_NAME_LENGTH, metaData.getMaxTableNameLength());
    }

    @Test
    void getDefaultTransactionIsolation() throws SQLException {
        assertEquals(YdbConst.TRANSACTION_SERIALIZABLE_READ_WRITE, metaData.getDefaultTransactionIsolation());
    }

    @Test
    void supportsTransactions() throws SQLException {
        assertTrue(metaData.supportsTransactions());
    }

    @Test
    void supportsTransactionIsolationLevel() throws SQLException {
        assertTrue(metaData.supportsTransactionIsolationLevel(YdbConst.TRANSACTION_SERIALIZABLE_READ_WRITE));
        assertTrue(metaData.supportsTransactionIsolationLevel(YdbConst.ONLINE_CONSISTENT_READ_ONLY));
        assertTrue(metaData.supportsTransactionIsolationLevel(YdbConst.ONLINE_INCONSISTENT_READ_ONLY));
        assertTrue(metaData.supportsTransactionIsolationLevel(YdbConst.STALE_CONSISTENT_READ_ONLY));

        assertFalse(metaData.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED));
        assertFalse(metaData.supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE));
    }

    @Test
    void supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        assertTrue(metaData.supportsDataDefinitionAndDataManipulationTransactions());
    }

    @Test
    void supportsDataManipulationTransactionsOnly() throws SQLException {
        assertTrue(metaData.supportsDataManipulationTransactionsOnly());
    }

    @Test
    void dataDefinitionCausesTransactionCommit() throws SQLException {
        assertFalse(metaData.dataDefinitionCausesTransactionCommit());
    }

    @Test
    void dataDefinitionIgnoredInTransactions() throws SQLException {
        assertTrue(metaData.dataDefinitionIgnoredInTransactions());
    }

    @Test
    void getProcedures() throws SQLException {
        checkNoResult(metaData.getProcedures(null, null, null));
    }

    @Test
    void getProcedureColumns() throws SQLException {
        checkNoResult(metaData.getProcedureColumns(null, null, null, null));
    }

    @Test
    void getTables() throws SQLException {
        YdbStatement statement = connection.createStatement();
        statement.executeSchemeQuery("create table t1 (id Int32, value Int32, primary key (id))");
        statement.executeSchemeQuery("create table `dir1/t1` (id Int32, value Int32, primary key (id))");
        statement.executeSchemeQuery("create table `dir1/t2` (id Int32, value Int32, primary key (id))");
        statement.executeSchemeQuery("create table `dir2/t1` (id Int32, value Int32, primary key (id))");
        statement.executeSchemeQuery("create table `dir2/dir1/t1` (id Int32, value Int32, primary key (id))");

        ResultSetData<Map<String, Map<String, Map<String, Object>>>> allSet =
                collectTables(metaData.getTables(null, null, null, null));
        Set<String> expectTableNames = set(
                "t1",
                "dir1/t1",
                "dir1/t2",
                "dir2/t1",
                "dir2/dir1/t1");

        Map<String, Map<String, Object>> tables = allSet.rows.get(TABLE);
        assertTrue(tables.keySet().containsAll(expectTableNames),
                () -> tables.keySet() + " must contains all elements of " + expectTableNames);

        Map<String, Map<String, Object>> expectTables = new LinkedHashMap<>(tables);
        expectTables.keySet().removeIf(name -> !expectTableNames.contains(name));

        checkResultSet("expect_tables", new ResultSetData<>(allSet.metadata, expectTables));

        Map<String, Map<String, Object>> systemTables = allSet.rows.get(SYSTEM_TABLE);
        assertNotEquals(Collections.emptySet(), systemTables);
        //

        ResultSetData<Map<String, Map<String, Map<String, Object>>>> tablesOnly =
                collectTables(metaData.getTables(null, null, null, new String[]{TABLE}));
        assertEquals(allSet.metadata, tablesOnly.metadata);
        assertEquals(allSet.rows.get(TABLE), tablesOnly.rows.get(TABLE));
        assertNull(tablesOnly.rows.get(SYSTEM_TABLE));

        ResultSetData<Map<String, Map<String, Map<String, Object>>>> systemTablesOnly =
                collectTables(metaData.getTables(null, null, null, new String[]{SYSTEM_TABLE}));
        assertEquals(allSet.metadata, systemTablesOnly.metadata);
        assertNull(systemTablesOnly.rows.get(TABLE));
        assertEquals(allSet.rows.get(SYSTEM_TABLE), systemTablesOnly.rows.get(SYSTEM_TABLE));

        ResultSetData<Map<String, Map<String, Map<String, Object>>>> allSetExact =
                collectTables(metaData.getTables(null, null, null, new String[]{TABLE, "some string", SYSTEM_TABLE}));
        assertEquals(allSet, allSetExact);

        ResultSetData<Map<String, Map<String, Map<String, Object>>>> nameOnly =
                collectTables(metaData.getTables(null, null, "dir1/t1", new String[]{TABLE}));
        assertEquals(allSet.metadata, nameOnly.metadata);
        assertEquals(set("dir1/t1"), nameOnly.rows.get(TABLE).keySet());
        assertNull(nameOnly.rows.get(SYSTEM_TABLE));

        ResultSetData<Map<String, Map<String, Map<String, Object>>>> nameOnlyButSystem =
                collectTables(metaData.getTables(null, null, "dir1/t1", new String[]{SYSTEM_TABLE}));
        checkEmptyMap(nameOnlyButSystem);

    }

    @Test
    void getTablesEmpty() throws SQLException {
        checkEmptyMap(collectTables(metaData.getTables("-", null, null, null)));
        checkEmptyMap(collectTables(metaData.getTables(null, "-", null, null)));
        checkEmptyMap(collectTables(metaData.getTables(null, "-", "unknown-table", null)));
        checkEmptyMap(collectTables(metaData.getTables(null, "-", null, new String[]{"U-1"})));
        checkEmptyMap(collectTables(metaData.getTables(null, "-", null, new String[0])));
    }

    @Test
    void getSchemas() throws SQLException {
        checkNoResult(metaData.getSchemas());
    }

    @Test
    void getCatalogs() throws SQLException {
        checkNoResult(metaData.getCatalogs());
    }

    @Test
    void getTableTypes() throws SQLException {
        ResultSetData<List<Map<String, Object>>> schemas = collectResultSet(metaData.getTableTypes());
        checkResultSet("table_types", schemas);
    }

    @Test
    void getColumns() throws SQLException {
        String singleTable = "unit_1";
        ResultSetData<Map<String, Map<String, Map<String, Object>>>> singleTableColumns =
                collectColumns(metaData.getColumns(null, null, singleTable, null));
        assertEquals(set(singleTable), singleTableColumns.rows.keySet());

        Map<String, Map<String, Object>> expectColumns = singleTableColumns.rows.get(singleTable);
        // GSON cannot deserialize numbers as integer/short, so let's compare objects as string

        // TODO: temporary until complete migration with docker
        checkResultSet("unit_1_columns", singleValue(singleTableColumns, singleTable), true);

        ResultSetData<Map<String, Map<String, Map<String, Object>>>> allColumns =
                collectColumns(metaData.getColumns(null, null, null, null));
        assertEquals(singleTableColumns.metadata, allColumns.metadata);
        assertEquals(expectColumns, allColumns.rows.get(singleTable));

        String testColumn = "c_JsonDocument";
        ResultSetData<Map<String, Map<String, Map<String, Object>>>> singleColumn =
                collectColumns(metaData.getColumns(null, null, singleTable, testColumn));
        assertEquals(set(testColumn), singleColumn.rows.get(singleTable).keySet());
        assertEquals(allColumns.metadata, singleColumn.metadata);
        assertEquals(expectColumns.get(testColumn),
                singleColumn.rows.get(singleTable).get(testColumn));
    }

    @Test
    void getColumnsEmpty() throws SQLException {
        checkEmptyMap(collectColumns(metaData.getColumns("-", null, null, null)));
        checkEmptyMap(collectColumns(metaData.getColumns(null, "-", null, null)));
        checkEmptyMap(collectColumns(metaData.getColumns(null, "-", "unknown-table", null)));
        checkEmptyMap(collectColumns(metaData.getColumns(null, "-", null, "x-column-unknown")));
    }

    @Test
    void getColumnPrivileges() throws SQLException {
        checkNoResult(metaData.getColumnPrivileges(null, null, null, null));
    }

    @Test
    void getTablePrivileges() throws SQLException {
        checkNoResult(metaData.getTablePrivileges(null, null, null));
    }

    @Test
    void getBestRowIdentifier() throws SQLException {
        String singleTable = "unit_1";
        ResultSetData<List<Map<String, Object>>> table1 =
                collectResultSet(metaData.getBestRowIdentifier(null, null, singleTable, bestRowSession, true));
        checkResultSet("unit_1_best_row", table1);
    }

    @Test
    void getBestRowIdentifierEmpty() throws SQLException {
        String singleTable = "unit_1";

        checkEmptyList(collectResultSet(metaData.getBestRowIdentifier("-", null, null, bestRowSession, true)));
        checkEmptyList(collectResultSet(metaData.getBestRowIdentifier(null, "-", null, bestRowSession, true)));
        checkEmptyList(collectResultSet(metaData.getBestRowIdentifier(null, null, "-", bestRowSession, true)));

        // expect exact column name
        checkEmptyList(collectResultSet(metaData.getBestRowIdentifier(null, null, null, bestRowSession, true)));

        // only nullable columns supported
        checkEmptyList(collectResultSet(metaData.getBestRowIdentifier(null, null, singleTable, bestRowSession, false)));

    }

    @Test
    void getVersionColumns() throws SQLException {
        checkNoResult(metaData.getVersionColumns(null, null, null));
    }

    @Test
    void getPrimaryKeys() throws SQLException {
        createTestTable("unit_1_multi_pk",
                "create table ${tableName} (key1 Int32, key2 Utf8, value Int32, primary key(key1, key2))");

        String singleTable1 = "unit_1";
        String singleTableMpk1 = "unit_1_multi_pk";

        ResultSetData<Map<String, Map<String, Map<String, Object>>>> columns1 =
                collectPrimaryKeys(metaData.getPrimaryKeys(null, null, singleTable1));
        assertEquals(set(singleTable1), columns1.rows.keySet());
        checkResultSet("unit_1_primary_keys", singleValue(columns1, singleTable1));

        ResultSetData<Map<String, Map<String, Map<String, Object>>>> columnsMpk1 =
                collectPrimaryKeys(metaData.getPrimaryKeys(null, null, singleTableMpk1));
        assertEquals(set(singleTableMpk1), columnsMpk1.rows.keySet());
        checkResultSet("unit_1_multi_pk_primary_keys", singleValue(columnsMpk1, singleTableMpk1));

        assertEquals(columns1.metadata, columnsMpk1.metadata);
    }

    @Test
    void getPrimaryKeysEmpty() throws SQLException {
        checkEmptyMap(collectPrimaryKeys(metaData.getPrimaryKeys("-", null, null)));
        checkEmptyMap(collectPrimaryKeys(metaData.getPrimaryKeys(null, "-", null)));
        checkEmptyMap(collectPrimaryKeys(metaData.getPrimaryKeys(null, null, "-")));

        // table name is a must
        checkEmptyMap(collectPrimaryKeys(metaData.getPrimaryKeys(null, null, null)));
    }

    @Test
    void getImportedKeys() throws SQLException {
        checkNoResult(metaData.getImportedKeys(null, null, null));
    }

    @Test
    void getExportedKeys() throws SQLException {
        checkNoResult(metaData.getExportedKeys(null, null, null));
    }

    @Test
    void getCrossReference() throws SQLException {
        checkNoResult(metaData.getCrossReference(null, null, null, null, null, null));
    }

    @Test
    void getTypeInfo() throws SQLException {
        ResultSetData<List<Map<String, Object>>> types = collectResultSet(metaData.getTypeInfo());
        checkResultSet("types", types);
    }

    @Test
    void getIndexInfo() throws SQLException {
        createTestTable("unit_1_multi_idx",
                "create table ${tableName} (key1 Int32, key2 Utf8, value1 Int32, value2 Utf8, value3 Int32, " +
                        "primary key(key1, key2), " +
                        "index idx_2 global on (value1, value2)," +
                        "index idx_1 global on (value3))");

        String singleTable1 = "unit_1";
        String singleTableMpi1 = "unit_1_multi_idx";

        ResultSetData<Map<String, List<Map<String, Object>>>> columns1 =
                collectIndexes(metaData.getIndexInfo(null, null, singleTable1, false, false));
        checkEmptyMap(columns1);

        ResultSetData<Map<String, List<Map<String, Object>>>> columnsMpi1 =
                collectIndexes(metaData.getIndexInfo(null, null, singleTableMpi1, false, false));
        assertEquals(set(singleTableMpi1), columnsMpi1.rows.keySet());
        checkResultSet("unit_1_multi_idx_indexes", singleValue(columnsMpi1, singleTableMpi1));
    }

    @Test
    void getIndexInfoEmpty() throws SQLException {
        checkEmptyMap(collectIndexes(metaData.getIndexInfo("-", null, null, false, false)));
        checkEmptyMap(collectIndexes(metaData.getIndexInfo(null, "-", null, false, false)));
        checkEmptyMap(collectIndexes(metaData.getIndexInfo(null, null, "-", false, false)));

        // no unique indexes
        checkEmptyMap(collectIndexes(metaData.getIndexInfo(null, null, null, true, false)));

        // table name is a must
        checkEmptyMap(collectIndexes(metaData.getIndexInfo(null, null, null, false, false)));
    }

    @Test
    void supportsResultSetType() throws SQLException {
        assertTrue(metaData.supportsResultSetType(TYPE_FORWARD_ONLY));
        assertTrue(metaData.supportsResultSetType(TYPE_SCROLL_INSENSITIVE));

        assertFalse(metaData.supportsResultSetType(TYPE_SCROLL_SENSITIVE));
    }

    @Test
    void supportsResultSetConcurrency() throws SQLException {
        assertTrue(metaData.supportsResultSetConcurrency(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY));
        assertTrue(metaData.supportsResultSetConcurrency(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY));

        assertFalse(metaData.supportsResultSetConcurrency(TYPE_SCROLL_SENSITIVE, CONCUR_READ_ONLY));
        assertFalse(metaData.supportsResultSetConcurrency(TYPE_FORWARD_ONLY, CONCUR_UPDATABLE));
        assertFalse(metaData.supportsResultSetConcurrency(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE));
    }

    @Test
    void ownUpdatesAreVisible() throws SQLException {
        assertFalse(metaData.ownUpdatesAreVisible(TYPE_SCROLL_INSENSITIVE));
    }

    @Test
    void ownDeletesAreVisible() throws SQLException {
        assertFalse(metaData.ownDeletesAreVisible(TYPE_SCROLL_INSENSITIVE));
    }

    @Test
    void ownInsertsAreVisible() throws SQLException {
        assertFalse(metaData.ownInsertsAreVisible(TYPE_SCROLL_INSENSITIVE));
    }

    @Test
    void othersUpdatesAreVisible() throws SQLException {
        assertFalse(metaData.othersUpdatesAreVisible(TYPE_SCROLL_INSENSITIVE));
    }

    @Test
    void othersDeletesAreVisible() throws SQLException {
        assertFalse(metaData.othersDeletesAreVisible(TYPE_SCROLL_INSENSITIVE));
    }

    @Test
    void othersInsertsAreVisible() throws SQLException {
        assertFalse(metaData.othersInsertsAreVisible(TYPE_SCROLL_INSENSITIVE));
    }

    @Test
    void updatesAreDetected() throws SQLException {
        assertFalse(metaData.updatesAreDetected(TYPE_SCROLL_INSENSITIVE));
    }

    @Test
    void deletesAreDetected() throws SQLException {
        assertFalse(metaData.deletesAreDetected(TYPE_SCROLL_INSENSITIVE));
    }

    @Test
    void insertsAreDetected() throws SQLException {
        assertFalse(metaData.insertsAreDetected(TYPE_SCROLL_INSENSITIVE));
    }

    @Test
    void supportsBatchUpdates() throws SQLException {
        assertTrue(metaData.supportsBatchUpdates());
    }

    @Test
    void getUDTs() throws SQLException {
        checkNoResult(metaData.getUDTs(null, null, null, null));
    }

    @Test
    void getConnection() throws SQLException {
        assertSame(connection, metaData.getConnection());
    }

    @Test
    void supportsSavepoints() throws SQLException {
        assertFalse(metaData.supportsSavepoints());
    }

    @Test
    void supportsNamedParameters() throws SQLException {
        assertFalse(metaData.supportsNamedParameters());
    }

    @Test
    void supportsMultipleOpenResults() throws SQLException {
        assertFalse(metaData.supportsMultipleOpenResults());
    }

    @Test
    void supportsGetGeneratedKeys() throws SQLException {
        assertFalse(metaData.supportsGetGeneratedKeys());
    }

    @Test
    void getSuperTypes() throws SQLException {
        checkNoResult(metaData.getSuperTypes(null, null, null));
    }

    @Test
    void getSuperTables() throws SQLException {
        checkNoResult(metaData.getSuperTables(null, null, null));
    }

    @Test
    void getAttributes() throws SQLException {
        checkNoResult(metaData.getAttributes(null, null, null, null));
    }

    @Test
    void supportsResultSetHoldability() throws SQLException {
        assertTrue(metaData.supportsResultSetHoldability(HOLD_CURSORS_OVER_COMMIT));
        assertFalse(metaData.supportsResultSetHoldability(CLOSE_CURSORS_AT_COMMIT));
    }

    @Test
    void getResultSetHoldability() throws SQLException {
        assertEquals(HOLD_CURSORS_OVER_COMMIT, metaData.getResultSetHoldability());
    }

    @Test
    void getDatabaseMajorVersion() throws SQLException {
        assertEquals(0, metaData.getDatabaseMajorVersion());
    }

    @Test
    void getDatabaseMinorVersion() throws SQLException {
        assertEquals(0, metaData.getDatabaseMinorVersion());
    }

    @Test
    void getJDBCMajorVersion() throws SQLException {
        assertEquals(YdbDriverInfo.JDBC_MAJOR_VERSION, metaData.getJDBCMajorVersion());
    }

    @Test
    void getJDBCMinorVersion() throws SQLException {
        assertEquals(YdbDriverInfo.JDBC_MINOR_VERSION, metaData.getJDBCMinorVersion());
    }

    @Test
    void getSQLStateType() throws SQLException {
        assertEquals(sqlStateSQL, metaData.getSQLStateType());
    }

    @Test
    void locatorsUpdateCopy() throws SQLException {
        assertFalse(metaData.locatorsUpdateCopy());
    }

    @Test
    void supportsStatementPooling() throws SQLException {
        assertTrue(metaData.supportsStatementPooling());
    }

    @Test
    void getRowIdLifetime() throws SQLException {
        assertEquals(RowIdLifetime.ROWID_UNSUPPORTED, metaData.getRowIdLifetime());
    }

    @Test
    void supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        assertFalse(metaData.supportsStoredFunctionsUsingCallSyntax());
    }

    @Test
    void autoCommitFailureClosesAllResultSets() throws SQLException {
        assertFalse(metaData.autoCommitFailureClosesAllResultSets());
    }

    @Test
    void getClientInfoProperties() throws SQLException {
        checkNoResult(metaData.getClientInfoProperties());
    }

    @Test
    void getFunctions() throws SQLException {
        checkNoResult(metaData.getFunctions(null, null, null));
    }

    @Test
    void getFunctionColumns() throws SQLException {
        checkNoResult(metaData.getFunctionColumns(null, null, null, null));
    }

    @Test
    void getPseudoColumns() throws SQLException {
        checkNoResult(metaData.getPseudoColumns(null, null, null, null));
    }

    @Test
    void generatedKeyAlwaysReturned() throws SQLException {
        assertFalse(metaData.generatedKeyAlwaysReturned());
    }

    @Test
    void unwrap() throws SQLException {
        assertTrue(metaData.isWrapperFor(YdbDatabaseMetaData.class));
        assertSame(metaData, metaData.unwrap(YdbDatabaseMetaData.class));

        assertFalse(metaData.isWrapperFor(YdbStatement.class));
        assertThrowsMsg(SQLException.class,
                () -> metaData.unwrap(YdbStatement.class),
                "Cannot unwrap to " + YdbStatement.class);
    }

    //

    private void checkNoResult(ResultSet rs) throws SQLException {
        assertFalse(rs.next());
    }


    private ResultSetData<List<Map<String, Object>>> collectResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        List<Map<String, Object>> metaMap = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            metaMap.add(stableMap(
                    "COLUMN_NAME", meta.getColumnName(i),
                    "COLUMN_TYPE", meta.getColumnType(i),
                    "COLUMN_TYPE_NAME", meta.getColumnTypeName(i)));
        }

        List<Map<String, Object>> rows = new ArrayList<>(16);
        while (rs.next()) {
            Map<String, Object> map = new LinkedHashMap<>(columnCount);
            rows.add(map);
            for (int i = 1; i <= columnCount; i++) {
                String columnName = meta.getColumnName(i);
                map.put(columnName, rs.getObject(i));
            }
        }
        return new ResultSetData<>(metaMap, rows);
    }

    private ResultSetData<Map<String, Map<String, Map<String, Object>>>> collectResultSet(
            ResultSet rs, String key1, String key2) throws SQLException {
        ResultSetData<List<Map<String, Object>>> result = collectResultSet(rs);

        Map<String, Map<String, Map<String, Object>>> map = new LinkedHashMap<>();
        for (Map<String, Object> table : result.rows) {
            String type = (String) table.get(key1);
            String name = (String) table.get(key2);
            map.computeIfAbsent(type, t -> new LinkedHashMap<>()).put(name, table);
        }
        return new ResultSetData<>(result.metadata, map);
    }

    // <Table type, <Table name, Table metadata>>
    private ResultSetData<Map<String, Map<String, Map<String, Object>>>> collectTables(
            ResultSet rs) throws SQLException {
        return collectResultSet(rs, "TABLE_TYPE", "TABLE_NAME");
    }

    // <Table name, <Column name, Column metadata>>
    private ResultSetData<Map<String, Map<String, Map<String, Object>>>> collectColumns(
            ResultSet rs) throws SQLException {
        return collectResultSet(rs, "TABLE_NAME", "COLUMN_NAME");
    }

    private ResultSetData<Map<String, Map<String, Map<String, Object>>>> collectPrimaryKeys(
            ResultSet rs) throws SQLException {
        return collectResultSet(rs, "TABLE_NAME", "COLUMN_NAME");
    }

    private ResultSetData<Map<String, List<Map<String, Object>>>> collectIndexes(
            ResultSet rs) throws SQLException {
        ResultSetData<List<Map<String, Object>>> result = collectResultSet(rs);
        Map<String, List<Map<String, Object>>> map = new LinkedHashMap<>();
        for (Map<String, Object> index : result.rows) {
            String type = (String) index.get("TABLE_NAME");
            map.computeIfAbsent(type, t -> new ArrayList<>()).add(index);
        }
        return new ResultSetData<>(result.metadata, map);
    }

    private String readJson(String resource) throws YdbConfigurationException {
        return YdbProperties.stringFileReference(resource);
    }

    private void checkEmptyMap(ResultSetData<? extends Map<?, ?>> resultSetData) {
        assertEquals(Collections.emptyMap(), resultSetData.rows);
        assertEquals(Collections.emptyList(), resultSetData.metadata);
    }

    private void checkEmptyList(ResultSetData<? extends List<?>> resultSetData) {
        assertEquals(Collections.emptyList(), resultSetData.rows);
        assertEquals(Collections.emptyList(), resultSetData.metadata);
    }

    private <T> void checkResultSet(String resource, ResultSetData<T> resultSetData) throws YdbConfigurationException {
        checkResultSet(resource, resultSetData, false);
    }

    private <T> void checkResultSet(String resource, ResultSetData<T> resultSetData, boolean tryNewVersion)
            throws YdbConfigurationException {
        assertEquals(readJson("classpath:json/" + resource + "_meta.json"), GSON.toJson(resultSetData.metadata));

        String expect = readJson("classpath:json/" + resource + ".json");
        String actual = GSON.toJson(resultSetData.rows);
        if (tryNewVersion) {
            if (!expect.equals(actual)) {
                assertEquals(readJson("classpath:json/" + resource + "-new.json"), actual);
            }
        } else {
            assertEquals(expect, actual);
        }
    }

    private static <T> ResultSetData<T> singleValue(ResultSetData<Map<String, T>> result, String key) {
        return new ResultSetData<>(result.metadata, result.rows.get(key));
    }

    private static class ResultSetData<T> {
        private final List<Map<String, Object>> metadata;
        private final T rows;

        private ResultSetData(List<Map<String, Object>> metadata, T rows) {
            this.metadata = Objects.requireNonNull(metadata);
            this.rows = Objects.requireNonNull(rows);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ResultSetData)) {
                return false;
            }
            ResultSetData<?> that = (ResultSetData<?>) o;
            return Objects.equals(metadata, that.metadata) &&
                    Objects.equals(rows, that.rows);
        }

        @Override
        public int hashCode() {
            return Objects.hash(metadata, rows);
        }

        @Override
        public String toString() {
            return "ResultSetData{" +
                    "metadata=" + metadata +
                    ", rows=" + rows +
                    '}';
        }
    }
}
