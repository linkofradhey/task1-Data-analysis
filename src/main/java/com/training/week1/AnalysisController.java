package com.training.week1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AnalysisController
 *
 * REST endpoints for the Week 1 data analysis task. Runs under the SAME
 * embedded Tomcat server as the static frontend (no separate server needed) -
 * everything is started by one "Run As -> Spring Boot App" in STS.
 *
 *   GET /api/summary  -> equivalent of df.describe()
 *   GET /api/missing  -> equivalent of df.isnull().sum()
 *   GET /api/dtypes   -> equivalent of df.dtypes
 *   GET /api/raw      -> raw rows, used by the frontend to draw charts
 */
@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final DataAnalysisService service;

    // Constructor injection - Spring automatically wires the service bean in
    public AnalysisController(DataAnalysisService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public List<AnalysisDtos.ColumnSummaryDto> summary() {
        return service.getSummary();
    }

    @GetMapping("/missing")
    public List<AnalysisDtos.MissingDto> missing() {
        return service.getMissing();
    }

    @GetMapping("/dtypes")
    public List<AnalysisDtos.DtypeDto> dtypes() {
        return service.getDtypes();
    }

    @GetMapping("/raw")
    public List<Map<String, String>> raw() {
        return service.getRaw();
    }
}
