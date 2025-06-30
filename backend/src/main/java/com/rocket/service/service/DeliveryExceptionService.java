package com.rocket.service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rocket.service.entity.DeliveryExceptionDto;
import com.rocket.service.repository.DeliveryExceptionRepository;

@Service
public class DeliveryExceptionService {
    
    @Autowired
    DeliveryExceptionRepository repositoryDeliveryException;

    public List<DeliveryExceptionDto> obtenerRazonesExcepcion(){
        return repositoryDeliveryException.findAll();
    }
}
