package com.portal.data.api.controller;

import com.portal.data.api.dto.response.ResponseApi;
import com.portal.data.api.service.MetadataService;
import com.portal.data.api.utils.DataUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/metadata")
@Tag(name = "Metadata Management")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/get-data")
    public ResponseEntity<?> getMetadata(
        @RequestParam(value = "", required = false) String region,
        @RequestParam(value = "", required = false) String pathData,
        @RequestParam(value = "", required = false) String source,
        @RequestParam(value = "bronze", required = false) String medallion
    ) {
        long start = System.currentTimeMillis();

        MetadataService.SearchResult resultsData = metadataService.getIndexElasticsearch(
            region, pathData, medallion, source
        );

        ResponseApi<MetadataService.SearchResult> responseApi = new ResponseApi<>(
                HttpStatus.OK.value(),
                (System.currentTimeMillis() - start) / 1000.0,
                HttpStatus.OK.name(),
                resultsData
        );

        return ResponseEntity.ok(responseApi);
    }

    @GetMapping("/count")
    public ResponseEntity<?> getTotalDataset(
        @RequestParam(value = "", required = false) String region,
        @RequestParam(value = "", required = false) String pathData,
        @RequestParam(value = "", required = false) String source,
        @RequestParam(value = "bronze", required = false) String medallion
    ) {
        long start = System.currentTimeMillis();

        long resultCount = metadataService.getTotalDataset(
            region, pathData, medallion, source
        );

        Map<String, Long> mapped = new HashMap<>();
        mapped.put("totalData", resultCount);

        ResponseApi<Map<String, Long>> responseApi = new ResponseApi<>(
                HttpStatus.OK.value(),
                (System.currentTimeMillis() - start) / 1000.0,
                HttpStatus.OK.name(),
                mapped
        );
        return ResponseEntity.ok(responseApi);
    }

    @GetMapping("/timeseries-by-source")
    public ResponseEntity<?> getTimeseriesBySource(
        @RequestParam(value = "", required = false) String source,
        @RequestParam(value = "", required = false) String startDate,
        @RequestParam(value = "", required = false) String endDate
    ) {
        long start_time = System.currentTimeMillis();

        if (startDate == null) {
            startDate = DataUtils.dateRange().get("startDate");
        }

        if (endDate == null){
            endDate = DataUtils.dateRange().get("endDate");
        }

        List<Map<String, Object>> results = metadataService.getTimeseriesBySource(
            source, startDate, endDate
        );
        ResponseApi<List<Map<String, Object>>> responseApi = new ResponseApi<>(
                HttpStatus.OK.value(),
                (System.currentTimeMillis() - start_time) / 1000.0,
                HttpStatus.OK.name(),
                results
        );
        return ResponseEntity.ok(responseApi);
    }
}
