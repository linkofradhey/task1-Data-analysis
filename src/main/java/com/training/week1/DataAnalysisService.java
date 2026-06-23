package com.training.week1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DataAnalysisService
 *
 * Loads the configured CSV file once when the Spring app starts
 * (@PostConstruct), then exposes the same analysis steps the Python
 * Pandas task asked for: summary, missing values, dtypes, raw rows.
 *
 * Marked @Service so Spring manages it as a singleton bean and can
 * inject it straight into the REST controller.
 */
@Service
public class DataAnalysisService {

    // Reads "app.dataset.path" from application.properties.
    // Defaults to sample_data.csv if not set.
    @Value("${app.dataset.path:sample_data.csv}")
    private String datasetPath;

    private CsvLoader dataset;

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
                DataSummary.NumericSummary s = DataSummary.summarizeNumeric(dataset, col);
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

    /** Equivalent of df.isnull().sum() */
    public List<AnalysisDtos.MissingDto> getMissing() {
        List<AnalysisDtos.MissingDto> result = new ArrayList<>();
        for (MissingValueDetector.MissingInfo m : MissingValueDetector.detect(dataset)) {
            result.add(new AnalysisDtos.MissingDto(m.column, m.missingCount, round2(m.missingPercent)));
        }
        return result;
    }

    /** Equivalent of df.dtypes */
    public List<AnalysisDtos.DtypeDto> getDtypes() {
        List<AnalysisDtos.DtypeDto> result = new ArrayList<>();
        Map<String, DataTypeAnalyzer.ColumnType> types = DataTypeAnalyzer.inferTypes(dataset);
        for (Map.Entry<String, DataTypeAnalyzer.ColumnType> e : types.entrySet()) {
            result.add(new AnalysisDtos.DtypeDto(e.getKey(), e.getValue().toString()));
        }
        return result;
    }

    /** Raw rows, used by the frontend to draw charts */
    public List<Map<String, String>> getRaw() {
        return dataset.rows;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
