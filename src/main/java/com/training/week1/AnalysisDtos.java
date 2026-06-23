package com.training.week1;

/**
 * Response DTOs (Data Transfer Objects).
 *
 * Spring Boot auto-converts these to JSON using Jackson under the hood
 * (no manual JSON string-building needed, unlike a plain-Java HTTP server).
 * Field names here become the JSON keys automatically.
 */
public class AnalysisDtos {

    public static class ColumnSummaryDto {
        public String column;
        public String type;       // "numeric" or "categorical"
        public int count;
        public Double mean;
        public Double std;
        public Double min;
        public Double max;
        public Double median;
        public Integer unique;
        public String mostFrequent;
        public Integer mostFrequentCount;
    }

    public static class MissingDto {
        public String column;
        public int missingCount;
        public double missingPercent;

        public MissingDto(String column, int missingCount, double missingPercent) {
            this.column = column;
            this.missingCount = missingCount;
            this.missingPercent = missingPercent;
        }
    }

    public static class DtypeDto {
        public String column;
        public String dtype;

        public DtypeDto(String column, String dtype) {
            this.column = column;
            this.dtype = dtype;
        }
    }
}
