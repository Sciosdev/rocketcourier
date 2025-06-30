package com.rocket.service.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.rocket.service.entity.RegistryDto;

@Repository
public interface RegistroRepository extends MongoRepository<RegistryDto, String>{	
	@Query(value = "{ 'idCarga' : ?0}")
    List<RegistryDto> findByRegistroCarga(Long idCarga);
	
	@Query(value = "{ 'order.orderKey' : ?0}")
    RegistryDto findByOrderKey(ObjectId orderKey);
}
