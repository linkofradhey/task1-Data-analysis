package com.training.week1.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.week1.Dto.AnalysisDtos;
import com.training.week1.Service.DataAnalysisService;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final DataAnalysisService service;

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
