package com.rocket.service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.rocket.service.entity.TipoCargaDto;

@Repository
public interface TipoCargaRepository extends MongoRepository<TipoCargaDto, Integer>{
    List<TipoCargaDto> findByValue(Integer value);
}
