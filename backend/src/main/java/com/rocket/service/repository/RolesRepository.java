package com.rocket.service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.rocket.service.entity.RolDto;

@Repository
public interface RolesRepository extends MongoRepository<RolDto, String>{
	
	@Query(value = "{ 'rol' : ?0}")
    List<RolDto> findByRol(String rol);
}
