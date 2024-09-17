package com.tourbackend.dataSet;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    private final NominatimService nominatimService;

    @Autowired
    public Controller(NominatimService nominatimService) {
        this.nominatimService = nominatimService;
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPlace(@RequestParam String placeName) {
        Map<String, Object> result = nominatimService.fetchAddressDetails(placeName);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
