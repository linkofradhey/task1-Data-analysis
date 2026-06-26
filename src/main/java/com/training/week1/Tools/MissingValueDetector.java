package com.training.week1.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.training.week1.Service.CsvLoader;

/**
 * MissingValueDetector - Equivalent of Python's df.isnull().sum()
 *
 * Counts how many empty/missing values exist in each column,
 * and the percentage of missing data — the same numbers you'd
 * see from df.isnull().sum() and (df.isnull().mean() * 100).
 */
public class MissingValueDetector {

    public static class MissingInfo {
        public String column;
        public int missingCount;
        public double missingPercent;

        public MissingInfo(String column, int missingCount, double missingPercent) {
            this.column = column;
            this.missingCount = missingCount;
            this.missingPercent = missingPercent;
        }
    }

    public static List<MissingInfo> detect(CsvLoader data) {
        List<MissingInfo> result = new ArrayList<>();
        int totalRows = data.rowCount();

        for (String col : data.headers) {
            int missing = 0;
            for (Map<String, String> row : data.rows) {
                String value = row.get(col);
                if (value == null || value.trim().isEmpty()) {
                    missing++;
                }
            }
            double percent = totalRows == 0 ? 0 : (missing * 100.0 / totalRows);
            result.add(new MissingInfo(col, missing, percent));
        }
        return result;
    }
}
