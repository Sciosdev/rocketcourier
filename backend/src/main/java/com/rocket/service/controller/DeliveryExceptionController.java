package com.rocket.service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.rocket.service.entity.DeliveryExceptionDto;
import com.rocket.service.service.DeliveryExceptionService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class DeliveryExceptionController {
    
    @Autowired
    DeliveryExceptionService deliveryExceptionService;

    @RequestMapping(value = "/deliveryException", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
    public ResponseEntity<List<DeliveryExceptionDto>> obtenerRazonesDeExcepcion(){

        List<DeliveryExceptionDto> reasons = deliveryExceptionService.obtenerRazonesExcepcion();

        log.info("Se obtienen las razones de excepción en la entrega");

        if(!reasons.isEmpty()){
            return new ResponseEntity<>(reasons, HttpStatus.OK );
        }

        return new ResponseEntity<>(reasons, HttpStatus.NO_CONTENT);
    }
}
