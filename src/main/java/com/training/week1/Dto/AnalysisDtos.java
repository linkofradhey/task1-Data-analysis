package com.training.week1.Dto;

import lombok.Getter;
import lombok.Setter;

public class AnalysisDtos {

    public static class ColumnSummaryDto {
        public String column;
        public String type;       
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
    
    @Getter	
    @Setter
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

    @Getter
    @Setter
    public static class DtypeDto {
        public String column;
        public String dtype;

        public DtypeDto(String column, String dtype) {
            this.column = column;
            this.dtype = dtype;
        }
    }
}
