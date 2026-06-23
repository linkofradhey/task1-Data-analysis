package com.training.week1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CsvLoader - Equivalent of Python's pandas.read_csv()
 *
 * Reads a CSV file into memory as a list of column names and
 * a list of row-maps (column -> value), keeping everything as
 * raw Strings at this stage (type inference happens later,
 * just like pandas infers dtypes after loading).
 */
public class CsvLoader {

    public List<String> headers = new ArrayList<>();
    public List<Map<String, String>> rows = new ArrayList<>();

    public void load(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        if (lines.isEmpty()) {
            throw new IOException("CSV file is empty: " + filePath);
        }

        // First line = header row
        headers = splitCsvLine(lines.get(0));

        // Remaining lines = data rows
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue; // skip blank lines

            List<String> values = splitCsvLine(line);
            Map<String, String> row = new LinkedHashMap<>();

            for (int col = 0; col < headers.size(); col++) {
                String value = (col < values.size()) ? values.get(col) : "";
                row.put(headers.get(col), value.trim());
            }
            rows.add(row);
        }
    }

    /**
     * Simple CSV line splitter. Handles basic comma-separated values.
     * (Doesn't handle quoted commas - fine for straightforward datasets;
     * mention to instructor if your real dataset has commas inside quoted fields.)
     */
    private List<String> splitCsvLine(String line) {
        return Arrays.asList(line.split(",", -1));
    }

    public int rowCount() {
        return rows.size();
    }

    public int colCount() {
        return headers.size();
    }
}
