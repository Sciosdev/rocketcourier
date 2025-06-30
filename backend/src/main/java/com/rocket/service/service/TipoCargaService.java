package com.rocket.service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rocket.service.entity.TipoCargaDto;
import com.rocket.service.repository.TipoCargaRepository;

@Service
public class TipoCargaService {
    @Autowired
    TipoCargaRepository tipoCargaRepository;

    public List<TipoCargaDto> obtenerTipoCarga() {

        List<TipoCargaDto> resultado = tipoCargaRepository.findAll();

        return resultado;

    }
}
