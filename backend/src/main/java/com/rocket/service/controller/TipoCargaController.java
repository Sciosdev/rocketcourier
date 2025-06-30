package com.rocket.service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.rocket.service.entity.TipoCargaDto;
import com.rocket.service.service.TipoCargaService;

import lombok.extern.slf4j.Slf4j;

@RestController @Slf4j
public class TipoCargaController {
    @Autowired
    TipoCargaService tipoCargaService;

    @RequestMapping(value = "tipo-carga/", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
    public ResponseEntity<String> getTipoCarga() {

        List<TipoCargaDto> tipoCarga = tipoCargaService.obtenerTipoCarga();

        log.info("Obteniendo el catálogo de tipo de carga");
		Gson gson = new Gson();
		return new ResponseEntity<>(gson.toJson(tipoCarga), HttpStatus.OK);

    }
}
