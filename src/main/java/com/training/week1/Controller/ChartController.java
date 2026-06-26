package com.training.week1.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.week1.Dto.AnalysisDtos;
import com.training.week1.Service.ChartService;
import com.training.week1.Service.DataAnalysisService;

@RestController
@RequestMapping("/api/charts")
public class ChartController {

    private final DataAnalysisService dataAnalysisService;
    private final ChartService chartService;

    public ChartController(DataAnalysisService dataAnalysisService) {
        this.dataAnalysisService = dataAnalysisService;
        this.chartService = new ChartService();
    }

    @GetMapping(value = "/bar/missing", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getMissingBarChart() throws IOException {
        List<AnalysisDtos.MissingDto> missingData = dataAnalysisService.getMissing();
        return chartService.generateBarChart(missingData, "Missing Values per Column");
    }

    @GetMapping(value = "/pie/department", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getDepartmentPieChart() throws IOException {
        List<Map<String, String>> rawData = dataAnalysisService.getRaw();
        return chartService.generatePieChart(rawData, "Department", "Employees by Department");
    }
}