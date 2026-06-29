package com.training.week1.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import com.training.week1.Dto.AnalysisDtos;

@Service
public class ChartService {

	public byte[] generateBarChart(List<AnalysisDtos.MissingDto> data, String title) throws IOException {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		data.forEach(dto -> dataset.addValue(dto.missingCount, "Count", dto.column));

		JFreeChart barChart = ChartFactory.createBarChart(title, 
				"Category", 
				"Value", 
				dataset);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ChartUtils.writeChartAsPNG(baos, barChart, 800, 500);
		return baos.toByteArray();
	}

	public byte[] generatePieChart(List<Map<String, String>> data, String categoryColumn, String title)
			throws IOException {

		Map<String, Integer> counts = new LinkedHashMap<>();
		data.forEach(row -> {
			String key = row.getOrDefault(categoryColumn, "Unknown");
			counts.merge(key, 1, Integer::sum);
		});

		DefaultPieDataset dataset = new DefaultPieDataset();
		counts.forEach((label, count) -> dataset.setValue(label, count));

		JFreeChart pieChart = ChartFactory.createPieChart(title, // Chart title
				dataset, // Data
				true, // Show legend
				true, // Show tooltips
				false // No URLs
		);

		// 4. Render to PNG bytes
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ChartUtils.writeChartAsPNG(baos, pieChart, 800, 500);
		return baos.toByteArray();
	}
}