package com.rocket.service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rocket.service.entity.ConfiguracionDto;
import com.rocket.service.repository.ConfiguracionRepository;
import com.rocket.service.utils.ConfiguracionKey;

@Service
public class ConfiguracionService {

    @Autowired
    ConfiguracionRepository configuracionRepository;

    public ConfiguracionDto obtenerConfiguracion(ConfiguracionKey key) {

        List<ConfiguracionDto> resultado = configuracionRepository.findByKey(key.getKey());

        if (resultado == null || resultado.isEmpty()) {
            return null;
        } else {
            return resultado.get(0);
        }

    }

}
