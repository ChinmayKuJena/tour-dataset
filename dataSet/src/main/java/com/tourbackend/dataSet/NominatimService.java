package com.tourbackend.dataSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class NominatimService {
    private final RestTemplate restTemplate;
    private final MongoTemplate mongoTemplate;

    @Value("${nominatim.api.url}")
    private String nominatimApiUrl;

    public NominatimService(RestTemplateBuilder restTemplateBuilder, MongoTemplate mongoTemplate) {
        this.restTemplate = restTemplateBuilder.build();
        this.mongoTemplate = mongoTemplate;
    }

    public Map<String, Object> fetchAddressDetails(String placeName) {
        String url = String.format("%s?q=%s&format=json&addressdetails=1", nominatimApiUrl, placeName);

        ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().length > 0) {
            Map<String, Object> place = (Map<String, Object>) response.getBody()[0];
            Map<String, Object> address = (Map<String, Object>) place.get("address");
            System.out.println(address);
            saveAddressDetails(placeName, address);
            return address;
        }

        return null; // return null if no data or error occurred
    }

    public void saveAddressDetails(String placeName, Map<String, Object> address) {
        if (address != null) {
            // Determine collection name based on the address fields
            String collectionName = determineCollectionName(address);
            if (collectionName != null) {
                // Save the address details in the appropriate collection
                mongoTemplate.save(address, collectionName);
                System.out.println("Saved address details to collection: " + collectionName);
            } else {
                System.out.println("Unable to determine collection name");
            }
        }
    }

    private String determineCollectionName(Map<String, Object> address) {
        // Iterate over the address map to determine the collection name
        if (address.containsKey("state_district")) {
            return "stateDistrict";
        } else if (address.containsKey("county")) {
            return "county";
        } else if (address.containsKey("state")) {
            return "state";
        } else if (address.containsKey("postcode")) {
            return "postcode";
        } else if (address.containsKey("country")) {
            return "town";
        }
        return "defaultCollection"; // Default collection name if no specific key is found
    }
}
