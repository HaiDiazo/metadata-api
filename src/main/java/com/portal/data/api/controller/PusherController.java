package com.portal.data.api.controller;

import com.portal.data.api.dto.requests.MetadataRequest;
import com.portal.data.api.dto.response.ResponseApi;
import com.portal.data.api.service.MetadataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pusher")
@Tag(name = "Metadata Pusher")
public class PusherController {

    private final MetadataService metadataService;

    public PusherController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @PostMapping("")
    public ResponseEntity<?> pusher(
        @RequestBody MetadataRequest metadataRequest
    ) {
        long start = System.currentTimeMillis();
        MetadataRequest result = metadataService.getStandarizationMetadata(metadataRequest);

        ResponseApi<MetadataRequest> responseApi = new ResponseApi<>(
                HttpStatus.OK.value(),
                (System.currentTimeMillis() - start) / 1000.0,
                HttpStatus.OK.name(),
                result
        );

        return ResponseEntity.ok(responseApi);
    }
}
