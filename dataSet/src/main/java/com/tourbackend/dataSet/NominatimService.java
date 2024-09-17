package com.tourbackend.dataSet;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.web.client.RestTemplateBuilder;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import java.util.HashMap;
// import java.util.Map;

// @Service
// public class NominatimService {
//     private final RestTemplate restTemplate;

//     @Value("${nominatim.api.url}")
//     private String nominatimApiUrl;

//     public NominatimService(RestTemplateBuilder restTemplateBuilder) {
//         this.restTemplate = restTemplateBuilder.build();
//     }

//     public Map<String, Object> fetchAddressDetails(String placeName) {
//         String url = String.format("%s?q=%s&format=json&addressdetails=1", nominatimApiUrl, placeName);

//         ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);

//         if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().length > 0) {
//             Map<String, Object> place = (Map<String, Object>) response.getBody()[0];
//             Map<String, Object> address = (Map<String, Object>) place.get("address");
//             System.out.println(address);
//             if (address != null) {
//                 for (Map.Entry<String, Object> entry : address.entrySet()) {
//                     if (entry.getValue().toString().equalsIgnoreCase(placeName)) {
//                         System.out.println("The place name matches the value for key: " + entry.getKey());
//                     }
//                 }
//             }
//             return address;
//         }

//         return null; // return null if no data or error occurred
//     }
// }
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class NominatimService {
    private final RestTemplate restTemplate;
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    @Value("${nominatim.api.url}")
    private String nominatimApiUrl;

    @Value("${mongodb.uri}")
    private String mongoUri;

    public NominatimService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.mongoClient = MongoClients.create(mongoUri);
        this.database = mongoClient.getDatabase("myDatabase"); // Use your database name
    }

    public Map<String, Object> fetchAddressDetails(String placeName) {
        String url = String.format("%s?q=%s&format=json&addressdetails=1", nominatimApiUrl, placeName);

        ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().length > 0) {
            Map<String, Object> place = (Map<String, Object>) response.getBody()[0];
            Map<String, Object> address = (Map<String, Object>) place.get("address");
            System.out.println("Address details: " + address);

            if (address != null) {
                // Find the key that matches the placeName
                String collectionName = null;
                for (Map.Entry<String, Object> entry : address.entrySet()) {
                    if (entry.getValue().toString().equalsIgnoreCase(placeName)) {
                        collectionName = entry.getKey();
                        break;
                    }
                }

                if (collectionName != null) {
                    // Create or get the collection with the dynamic name
                    MongoCollection<Document> collection = database.getCollection(collectionName);

                    // Create a document to insert or update
                    Document document = new Document("placeName", placeName);
                    document.putAll(address);

                    // Insert or update the document
                    collection.updateOne(
                            new Document("placeName", placeName), // Query
                            new Document("$set", document), // Update
                            new UpdateOptions().upsert(true) // Upsert
                    );

                    System.out.println("Document inserted/updated in collection: " + collectionName);
                } else {
                    System.out.println("No matching key found for placeName: " + placeName);
                }
            }

            return address;
        }

        return null; // return null if no data or error occurred
    }
}
