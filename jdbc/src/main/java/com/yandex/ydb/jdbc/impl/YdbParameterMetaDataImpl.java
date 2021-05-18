package com.yandex.ydb.jdbc.impl;

import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.yandex.ydb.jdbc.YdbParameterMetaData;
import com.yandex.ydb.table.values.Type;

import static com.yandex.ydb.jdbc.YdbConst.NOTHING_TO_UNWRAP;
import static com.yandex.ydb.jdbc.YdbConst.PARAMETER_NOT_FOUND;
import static com.yandex.ydb.jdbc.YdbConst.PARAMETER_NUMBER_NOT_FOUND;

public class YdbParameterMetaDataImpl implements YdbParameterMetaData {

    private final Configuration cfg;

    public YdbParameterMetaDataImpl(Map<String, Type> types) {
        this.cfg = asConfiguration(types);
    }

    @Override
    public int getParameterCount() {
        return cfg.descriptions.length;
    }

    @Override
    public int isNullable(int param) throws SQLException {
        return getDescription(param).optional ?
                ParameterMetaData.parameterNullable :
                ParameterMetaData.parameterNoNulls;
    }

    @Override
    public boolean isSigned(int param) {
        return false; // TODO: support?
    }

    @Override
    public int getPrecision(int param) {
        return 0; // TODO: support?
    }

    @Override
    public int getScale(int param) {
        return 0; // TODO: support?
    }

    @Override
    public int getParameterType(int param) throws SQLException {
        return getDescription(param).sqlTypes.getSqlType();
    }

    @Override
    public String getParameterTypeName(int param) throws SQLException {
        return getDescription(param).sqlTypes.getDatabaseType();
    }

    @Override
    public String getParameterClassName(int param) throws SQLException {
        return getDescription(param).sqlTypes.getJavaType().getName();
    }

    @Override
    public int getParameterMode(int param) {
        return parameterModeIn; // Only in is supported
    }

    @Override
    public int getParameterIndex(String parameterName) throws SQLException {
        Integer index = cfg.indexes.get(parameterName);
        if (index == null) {
            throw new SQLException(PARAMETER_NOT_FOUND + parameterName);
        }
        return index + 1;
    }

    @Override
    public String getParameterName(int param) throws SQLException {
        return cfg.names[getIndex(param)];
    }

    private int getIndex(int param) throws SQLException {
        if (param <= 0 || param > cfg.descriptions.length) {
            throw new SQLException(PARAMETER_NUMBER_NOT_FOUND + param);
        }
        return param - 1;
    }

    private TypeDescription getDescription(int param) throws SQLException {
        return cfg.descriptions[getIndex(param)];
    }

    private static Configuration asConfiguration(Map<String, Type> types) {
        // TODO: cache?
        int count = types.size();
        TypeDescription[] descriptions = new TypeDescription[count];
        String[] names = new String[count];
        Map<String, Integer> indexes = new HashMap<>(count);
        int index = 0;
        for (Map.Entry<String, Type> entry : types.entrySet()) {
            descriptions[index] = TypeDescription.of(entry.getValue());
            names[index] = entry.getKey();
            indexes.put(entry.getKey(), index);
            index++;
        }
        return new Configuration(descriptions, names, indexes);
    }

    private static class Configuration {
        private final TypeDescription[] descriptions;
        private final String[] names;
        private final Map<String, Integer> indexes;

        private Configuration(TypeDescription[] descriptions, String[] names, Map<String, Integer> indexes) {
            this.descriptions = Objects.requireNonNull(descriptions);
            this.names = Objects.requireNonNull(names);
            this.indexes = Objects.requireNonNull(indexes);
            Preconditions.checkState(descriptions.length == names.length);
            Preconditions.checkState(descriptions.length == indexes.size());
        }
    }


    // UNSUPPORTED

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException(NOTHING_TO_UNWRAP);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }
}
