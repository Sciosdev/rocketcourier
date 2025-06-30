package com.rocket.service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.rocket.service.entity.VendorDto;

@Repository
public interface TiendaRepository extends MongoRepository<VendorDto, String>{	
	@Query(value = "{ '_id' : ?0}")
    List<VendorDto> findByIdTienda(String id);
}
