package com.rocket.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.rocket.service.entity.DeliveryExceptionDto;

@Repository
public interface DeliveryExceptionRepository extends MongoRepository<DeliveryExceptionDto, String>{
}
