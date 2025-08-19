package com.portal.data.api.controller;

import com.portal.data.api.dto.requests.MetadataRequest;
import com.portal.data.api.dto.response.ResponseApi;
import com.portal.data.api.service.MetadataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;

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
       @Valid @RequestBody MetadataRequest metadataRequest
    ) {
        long start = System.currentTimeMillis();
        MetadataService.MetadataReasonStandarization result = null;
        try {
            result = metadataService.getStandarizationMetadata(metadataRequest);
            if (result.isStandarization()) {
                ResponseApi<MetadataRequest> responseApi = new ResponseApi<>(
                        HttpStatus.OK.value(),
                        (System.currentTimeMillis() - start) / 1000.0,
                        HttpStatus.OK.name(),
                        result.metadataRequest()
                );
                return ResponseEntity.ok(responseApi);
            }

            ResponseApi<MetadataRequest> responseApi = new ResponseApi<>(
                    HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    (System.currentTimeMillis() - start) / 1000.0,
                    result.reasonStandarization(),
                    result.metadataRequest()
            );
            return ResponseEntity.unprocessableEntity().body(responseApi);

        } catch (IOException e) {

            ResponseApi<MetadataRequest> responseApi = new ResponseApi<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    (System.currentTimeMillis() - start) / 1000.0,
                    e.getMessage() + " " + e.getCause(),
                    null
            );
            return ResponseEntity.internalServerError().body(responseApi);
        }
    }
}
