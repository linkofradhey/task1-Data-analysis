package com.training.week1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class DataSummary {

	public static class NumericSummary {
		public String column;
		public int count;
		public double mean;
		public double std;
		public double min;
		public double max;
		public double median;

		public NumericSummary(String column) {
			this.column = column;
		}
	}

	public static class CategoricalSummary {
		public String column;
		public int count;
		public int uniqueValues;
		public String mostFrequent;
		public int mostFrequentCount;

		public CategoricalSummary(String column) {
			this.column = column;
		}
	}

	public static NumericSummary summarizeNumeric(CsvLoader data, String column) {
		List<Double> values = new ArrayList<>();// here we have logic for std, mean max min
		for (Map<String, String> row : data.rows) {
			String v = row.get(column);
			if (v != null && !v.isEmpty()) {
				try {
					values.add(Double.parseDouble(v));
				} catch (NumberFormatException ignored) {
				}
			}
		}

		NumericSummary s = new NumericSummary(column);
		s.count = values.size();
		if (s.count == 0)
			return s;

		double sum = 0;
		double min = values.get(0);
		double max = values.get(0);
		for (double v : values) {
			sum += v;
			if (v < min)
				min = v;
			if (v > max)
				max = v;
		}
		s.mean = sum / s.count;
		s.min = min;
		s.max = max;

		double sqDiffSum = 0;
		for (double v : values) {
			sqDiffSum += Math.pow(v - s.mean, 2);
		}
		s.std = s.count > 1 ? Math.sqrt(sqDiffSum / (s.count - 1)) : 0;

		List<Double> sorted = new ArrayList<>(values);
		Collections.sort(sorted);
		int n = sorted.size();
		s.median = (n % 2 == 0) ? (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0 : sorted.get(n / 2);

		return s;
	}

	// ═══════════════════════════════════════════════════════════
//  Unified wrapper — holds either numeric or categorical result
//  ExportService iterates List<ColumnResult> directly
// ═══════════════════════════════════════════════════════════
	public static class ColumnResult {
		public String column;
		public String type; // "numeric" or "categorical"
		public NumericSummary numeric; // non-null only if type = numeric
		public CategoricalSummary categorical; // non-null only if type = categorical
	}

	public static CategoricalSummary summarizeCategorical(CsvLoader data, String column) {
		Map<String, Integer> freq = new LinkedHashMap<>();
		int count = 0;// data type

		for (Map<String, String> row : data.rows) {
			String v = row.get(column);
			if (v != null && !v.isEmpty()) {
				count++;
				freq.merge(v, 1, Integer::sum);
			}
		}

		CategoricalSummary s = new CategoricalSummary(column);
		s.count = count;
		s.uniqueValues = freq.size();

		String topValue = null;
		int topCount = 0;
		for (Map.Entry<String, Integer> e : freq.entrySet()) {
			if (e.getValue() > topCount) {
				topCount = e.getValue();
				topValue = e.getKey();
			}
		}
		s.mostFrequent = topValue;
		s.mostFrequentCount = topCount;

		return s;
	}

	// ═══════════════════════════════════════════════════════════
//  summarizeAll() — detects column type and delegates
//
//  HOW TYPE IS DETECTED:
//    Tries to parse the first non-empty value as Double.
//    If it succeeds → numeric, else → categorical.
// ═══════════════════════════════════════════════════════════
	public static List<ColumnResult> summarizeAll(CsvLoader data) {
		List<ColumnResult> results = new ArrayList<>();

		if (data.rows == null || data.rows.isEmpty())
			return results;

		// Get all column names from the first row's key set
		for (String column : data.rows.get(0).keySet()) {

			ColumnResult result = new ColumnResult();
			result.column = column;

			if (isNumericColumn(data, column)) {
				result.type = "numeric";
				result.numeric = summarizeNumeric(data, column);
			} else {
				result.type = "categorical";
				result.categorical = summarizeCategorical(data, column);
			}

			results.add(result);
		}

		return results;
	}

// ── Helper: detect if a column is numeric ──────────────────
	private static boolean isNumericColumn(CsvLoader data, String column) {
		for (Map<String, String> row : data.rows) {
			String v = row.get(column);
			if (v != null && !v.trim().isEmpty()) {
				try {
					Double.parseDouble(v.trim());
					return true; // first parseable value → numeric
				} catch (NumberFormatException e) {
					return false; // first parseable value → categorical
				}
			}
		}
		return false; // all values empty → treat as categorical
	}
}
