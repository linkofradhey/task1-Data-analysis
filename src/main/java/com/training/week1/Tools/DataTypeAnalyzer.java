package com.training.week1.Tools;

import java.util.LinkedHashMap;
import java.util.Map;

import com.training.week1.Service.CsvLoader;


public class DataTypeAnalyzer {

    public enum ColumnType { INTEGER, FLOAT, DATE, TEXT, EMPTY }

    
    public static Map<String, ColumnType> inferTypes(CsvLoader data) {
        Map<String, ColumnType> types = new LinkedHashMap<>();

        for (String col : data.headers) {
            types.put(col, inferColumnType(data, col));
        }
        return types;
    }

    private static ColumnType inferColumnType(CsvLoader data, String column) {
        boolean allInt = true;
        boolean allFloat = true;
        boolean allDate = true;
        int nonEmptyCount = 0;

        for (Map<String, String> row : data.rows) {
            String value = row.get(column);
            if (value == null || value.isEmpty()) continue; // skip blanks for type check

            nonEmptyCount++;

            if (allInt && !isInteger(value)) allInt = false;
            if (allFloat && !isFloat(value)) allFloat = false;
            if (allDate && !isDate(value)) allDate = false;
        }

        if (nonEmptyCount == 0) return ColumnType.EMPTY;
        if (allInt) return ColumnType.INTEGER;
        if (allFloat) return ColumnType.FLOAT;
        if (allDate) return ColumnType.DATE;
        return ColumnType.TEXT;
    }

    private static boolean isInteger(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isFloat(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDate(String value) {
        // Matches yyyy-MM-dd format, e.g. 2021-03-15
        return value.matches("\\d{4}-\\d{2}-\\d{2}");
    }
}
