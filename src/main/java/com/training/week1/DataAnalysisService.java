package com.training.week1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;


@Service
public class DataAnalysisService {

    // Reads "app.dataset.path" from application.properties.
    // Defaults to sample_data.csv if not set.
//    @Value("${app.dataset.path:sample_data.csv}")
	
	@Value("${app.dataset.path:sample_data.csv}")
    private String datasetPath;

    private CsvLoader dataset;

    public DataAnalysisService(CsvLoader csvLoader) {
        this.dataset = csvLoader;
    }
    public List<String> getHeaders() {
        if (dataset.rows == null || dataset.rows.isEmpty()) 
            return Collections.emptyList();
        return new ArrayList<>(dataset.rows.get(0).keySet());
    }
    
    // ── Returns unified list: each entry knows its column name,
    //    type, and stats — ExportService builds headers from this ──
//    public List<DataSummary.ColumnResult> getDataSummary() {
//        return DataSummary.summarizeAll(dataset);  // ← your existing static method
//    }

    // ── Raw rows — ExportService reads keys of first row as headers ──
    public List<Map<String, String>> getRaw() {
        return Collections.unmodifiableList(dataset.rows);
    }

    // ── Missing value counts per column ──
//    public Map<String, Integer> getMissing() {
//        Map<String, Integer> result = new LinkedHashMap<>();
//        for (String col : dataset.rows.get(0).keySet()) {
//            int missing = (int) dataset.rows.stream()
//                .filter(r -> r.get(col) == null || r.get(col).isEmpty())
//                .count();
//            result.put(col, missing);
//        }
//        return result;
//    }
//
//    // ── Data type per column ──
//    public Map<String, String> getDtypes() {
//        Map<String, String> result = new LinkedHashMap<>();
//        for (String col : dataset.rows.get(0).keySet()) {
//            result.put(col, DataTypeDetector.detect(dataset, col)); // your existing logic
//        }
//        return result;
//    }

    @PostConstruct
    public void init() throws IOException {
        dataset = new CsvLoader();
        dataset.load(datasetPath);
        System.out.println("Loaded dataset: " + datasetPath);
        System.out.println("Rows: " + dataset.rowCount() + ", Columns: " + dataset.colCount());
    }

    /** Equivalent of df.describe() */
    public List<AnalysisDtos.ColumnSummaryDto> getSummary() {
        Map<String, DataTypeAnalyzer.ColumnType> types = DataTypeAnalyzer.inferTypes(dataset);
        List<AnalysisDtos.ColumnSummaryDto> result = new ArrayList<>();

        for (String col : dataset.headers) {
            DataTypeAnalyzer.ColumnType type = types.get(col);
            AnalysisDtos.ColumnSummaryDto dto = new AnalysisDtos.ColumnSummaryDto();
            dto.column = col;

            if (type == DataTypeAnalyzer.ColumnType.INTEGER || type == DataTypeAnalyzer.ColumnType.FLOAT) {
                DataSummary.NumericSummary s = DataSummary.summarizeNumeric(dataset, col);//gets the data and load then in the dto
                dto.type = "numeric";
                dto.count = s.count;
                dto.mean = round2(s.mean);
                dto.std = round2(s.std);
                dto.min = round2(s.min);
                dto.max = round2(s.max);
                dto.median = round2(s.median);
            } else {
                DataSummary.CategoricalSummary s = DataSummary.summarizeCategorical(dataset, col);
                dto.type = "categorical";
                dto.count = s.count;
                dto.unique = s.uniqueValues;
                dto.mostFrequent = s.mostFrequent;
                dto.mostFrequentCount = s.mostFrequentCount;
            }
            result.add(dto);
        }
        return result;
    }
    public List<DataSummary.ColumnResult> getDataSummary() {
        return DataSummary.summarizeAll(dataset);
    }
    
    public List<AnalysisDtos.MissingDto> getMissing() {
        List<AnalysisDtos.MissingDto> result = new ArrayList<>();
        for (MissingValueDetector.MissingInfo m : MissingValueDetector.detect(dataset)) {
            result.add(new AnalysisDtos.MissingDto(m.column, m.missingCount, round2(m.missingPercent)));
        }
        return result;
    }

   
    public List<AnalysisDtos.DtypeDto> getDtypes() {
        List<AnalysisDtos.DtypeDto> result = new ArrayList<>();
        Map<String, DataTypeAnalyzer.ColumnType> types = DataTypeAnalyzer.inferTypes(dataset);
        for (Map.Entry<String, DataTypeAnalyzer.ColumnType> e : types.entrySet()) {
            result.add(new AnalysisDtos.DtypeDto(e.getKey(), e.getValue().toString()));
        }
        return result;
    }

    
//    public List<Map<String, String>> getRaw() {
//        return dataset.rows;
//    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;//to have only two decimal
    }
}
