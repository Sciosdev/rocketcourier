package com.rocket.service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.rocket.service.entity.LoadDto;

@Repository
public interface CargaRepository extends MongoRepository<LoadDto, String> {	
	@Query(value = "{ 'idVendor' : ?0}")
    List<LoadDto> findByIdVendor(String idVendor);
}