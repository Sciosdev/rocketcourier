package com.rocket.service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.rocket.service.entity.EstatusDto;

@Repository
public interface EstatusRepository extends MongoRepository<EstatusDto, String> {	
	@Query(value = "{ 'tipo' : ?0}")
    List<EstatusDto> findByTipo(String tipo);
}
