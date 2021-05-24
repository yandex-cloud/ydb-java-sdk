package org.springframework.jdbc.core.namedparam;

import java.util.List;
import java.util.Objects;

public class ParsedSqlAccessor {

    private final ParsedSql parsedSql;

    public ParsedSqlAccessor(ParsedSql parsedSql) {
        this.parsedSql = Objects.requireNonNull(parsedSql);
    }

    public String getOriginalSql() {
        return parsedSql.getOriginalSql();
    }

    public List<String> getParameterNames() {
        return parsedSql.getParameterNames();
    }

    public int[] getParameterIndexes(int parameterPosition) {
        return parsedSql.getParameterIndexes(parameterPosition);
    }

}
