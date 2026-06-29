package com.training.week1.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.week1.Dto.AnalysisDtos;
import com.training.week1.Dto.EditedDataRequest;
import com.training.week1.Service.DataAnalysisService;

@RestController
@RequestMapping("/api")
public class AnalysisController {
	@Autowired
    private  DataAnalysisService service;

//    public AnalysisController(DataAnalysisService service) {
//        this.service = service;
//    }

    @GetMapping("/summary")
    public List<AnalysisDtos.ColumnSummaryDto> summary() {
        return service.getSummary();
    }
 // In your controller
    @PostMapping("/api/data/save")
    public ResponseEntity<String> saveEditedData(@RequestBody EditedDataRequest editedData) {
    	service.overwriteCsv(editedData.getHeaders(), editedData.getRows());
        return ResponseEntity.ok("Data saved successfully");
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
