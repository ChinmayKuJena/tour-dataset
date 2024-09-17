package com.tourbackend.dataSet;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends MongoRepository<Place, String> {
}