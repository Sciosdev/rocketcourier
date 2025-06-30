package com.rocket.service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.rocket.service.entity.UserDto;

@Repository
public interface UserRepository extends MongoRepository<UserDto, String>{
	@Query(value = "{ 'user' : ?0}")
    List<UserDto> findByUser(String username);
	
	@Query(value = "{ 'rol' : ?0}")
    List<UserDto> findByUserForRol(String rol);
}
