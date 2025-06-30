package com.rocket.service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.rocket.service.entity.ConfiguracionDto;

@Repository
public interface ConfiguracionRepository extends MongoRepository<ConfiguracionDto, String>{
    List<ConfiguracionDto> findByKey(String key);
}
