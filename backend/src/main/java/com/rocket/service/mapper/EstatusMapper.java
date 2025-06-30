package com.rocket.service.mapper;

import com.rocket.service.entity.EstatusDto;
import com.rocket.service.entity.EstatusLogDto;
import com.rocket.service.model.EstatusLogDataDto;

public class EstatusMapper {

    public static EstatusLogDataDto estatusLogDtoToEstatusLogServiceDto (EstatusLogDto estatusLog, EstatusDto estatus){

        EstatusLogDataDto result = new EstatusLogDataDto();
   
        result.setEstatus(estatus.getDesc());
        result.setFecha(estatusLog.getFechaActualizacion());
        result.setUsuario(estatusLog.getUsuario());
        result.setTipoEstatus(estatus.getTipo());

        return result;
    }
    
}
