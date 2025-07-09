package com.rocket.service.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.rocket.service.entity.EstatusDto;
import com.rocket.service.entity.LoadDto;
import com.rocket.service.entity.RegistryDto;
import com.rocket.service.entity.RolDto;
import com.rocket.service.entity.UserDto;
import com.rocket.service.mapper.RegistroMapper;
import com.rocket.service.model.DBResponse;
import com.rocket.service.model.GroupingEstatusRegistroServiceOutDto;
import com.rocket.service.model.RegistroServiceOutDto;
import com.rocket.service.service.CargaService;
import com.rocket.service.service.EstatusService;
import com.rocket.service.service.RegistroService;
import com.rocket.service.service.RolService;
import com.rocket.service.service.UsuarioService;
import org.bson.types.ObjectId;
import com.rocket.service.service.VendorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistroController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    RolService rolService;

    @Autowired
    RegistroService registroService;

    @Autowired
    CargaService cargaService;

    @Autowired
    EstatusService estatusService;

    @Autowired
    VendorService vendorService;

    @RequestMapping(value = "/registro/list/{courier}", method = RequestMethod.GET, produces = {
            "application/json;charset=UTF-8" })
    private @ResponseBody ResponseEntity<String> obtenerRegistros(@PathVariable String courier) {
        List<RegistroServiceOutDto> registros = new ArrayList<RegistroServiceOutDto>();
        Gson gson = new Gson();
        String json = "";

        List<RegistryDto> temp = registroService.consultaRegistroPorCourier(courier);
        temp.forEach(registro -> {
            LoadDto carga = cargaService.obtenerCargaPorId(registro.getIdCarga());
            registros.add(RegistroMapper.mapRegistroCargaToRegistroOut(registro, carga, usuarioService,
                    vendorService, estatusService));
        });

        Map<String, List<RegistroServiceOutDto>> grupoRegistros = registros.stream()
                .collect(Collectors.groupingBy(a -> a.getDescStatus()));

        List<GroupingEstatusRegistroServiceOutDto> result = new ArrayList<>();

        List<UserDto> usuarioList = usuarioService.consulta(courier);
        if (usuarioList.isEmpty())
            return new ResponseEntity<>("Usuario: " + courier + " no encontrado.", HttpStatus.BAD_REQUEST);

        UserDto usuario = usuarioList.get(0);

        List<RolDto> roles = rolService.consultaRol(usuario.getRol());

        if (roles.isEmpty())
            return new ResponseEntity<>("Rol: " + usuario.getRol() + " no encontrado.", HttpStatus.BAD_REQUEST);

        RolDto rol = roles.get(0);

        List<EstatusDto> estatusList = estatusService.obtenerListaEstatus();

        List<EstatusDto> estatusListResult = new ArrayList<EstatusDto>();

        for (EstatusDto estatus : estatusList) {
            if (rol.getStatusView().contains(estatus.getId()))
                estatusListResult.add(estatus);
        }

        estatusListResult.forEach(key -> {
            GroupingEstatusRegistroServiceOutDto groupingEstatusRegistroServiceOutDto = new GroupingEstatusRegistroServiceOutDto();
            groupingEstatusRegistroServiceOutDto.setEstatus(key.getDesc());
            groupingEstatusRegistroServiceOutDto.setId(key.getId());
            groupingEstatusRegistroServiceOutDto.setTipo(key.getTipo());
            if (grupoRegistros.get(key.getDesc()) != null) {
                groupingEstatusRegistroServiceOutDto.setTotal(grupoRegistros.get(key.getDesc()).size());
                groupingEstatusRegistroServiceOutDto.setData(grupoRegistros.get(key.getDesc()));
            } else {
                groupingEstatusRegistroServiceOutDto.setTotal(0);
                groupingEstatusRegistroServiceOutDto.setData(new ArrayList<>());
            }

            result.add(groupingEstatusRegistroServiceOutDto);
        });

        json = gson.toJson(result);
        // json = gson.toJson(registros);
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    @RequestMapping(value = "/registro/{orderKey}/{courier}", method = RequestMethod.GET, produces = {
            "application/json;charset=UTF-8" })
    private @ResponseBody ResponseEntity<String> obtenerRegistro(@PathVariable String courier,
            @PathVariable String orderKey) {
        RegistroServiceOutDto registro = new RegistroServiceOutDto();
        Gson gson = new Gson();
        String json = "";

        RegistryDto temp = registroService.consultaRegistroPorCourierYOrderkey(courier, orderKey);

        if (temp == null) {

            DBResponse response = new DBResponse(false, "Registro no encontrado");

            json = gson.toJson(response);
            return new ResponseEntity<>(json, HttpStatus.BAD_REQUEST);
        }

        EstatusDto currentEstatus = estatusService.obtenerEstatusPorId(temp.getIdEstatus());

        LoadDto carga = cargaService.obtenerCargaPorId(temp.getIdCarga());
        registro = RegistroMapper.mapRegistroCargaToRegistroOut(temp, carga, usuarioService,
                vendorService, estatusService);

        GroupingEstatusRegistroServiceOutDto groupingEstatusRegistroServiceOutDto = new GroupingEstatusRegistroServiceOutDto();
        groupingEstatusRegistroServiceOutDto.setEstatus(currentEstatus.getDesc());
        groupingEstatusRegistroServiceOutDto.setId(currentEstatus.getId());
        groupingEstatusRegistroServiceOutDto.setTipo(currentEstatus.getTipo());
        groupingEstatusRegistroServiceOutDto.setTotal(1);
        
        
        List<RegistroServiceOutDto> data = new ArrayList<>();
        data.add(registro);
        groupingEstatusRegistroServiceOutDto.setData(data);

        

        json = gson.toJson(groupingEstatusRegistroServiceOutDto);
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    @RequestMapping(value = "/registro/{orderKey}", method = RequestMethod.DELETE, produces = {
            "application/json;charset=UTF-8" })
    public ResponseEntity<String> eliminarRegistro(@PathVariable String orderKey) {
        Gson gson = new Gson();
        DBResponse response;
        try {
            boolean eliminado = registroService.eliminarRegistro(new ObjectId(orderKey));
            if (eliminado) {
                response = new DBResponse(true, "Registro eliminado");
            } else {
                response = new DBResponse(false, "Registro no encontrado");
            }
        } catch (Exception e) {
            response = new DBResponse(false, "Error al eliminar el registro");
        }
        return new ResponseEntity<>(gson.toJson(response), HttpStatus.OK);
    }
}
