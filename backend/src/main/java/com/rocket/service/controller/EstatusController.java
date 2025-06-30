package com.rocket.service.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.rocket.service.entity.ConfiguracionDto;
import com.rocket.service.entity.EstatusDto;
import com.rocket.service.entity.EstatusLogDto;
import com.rocket.service.entity.LoadDto;
import com.rocket.service.entity.RegistryDto;
import com.rocket.service.entity.RolDto;
import com.rocket.service.entity.ScheduledDto;
import com.rocket.service.entity.UserDto;
import com.rocket.service.mapper.RegistroMapper;
import com.rocket.service.model.ActualizaEstatusEntregaMultipleOrdersServiceInDto;
import com.rocket.service.model.ActualizaEstatusOrderMultipleServiceInDto;
import com.rocket.service.model.ActualizaEstatusServiceInDto;
import com.rocket.service.model.ActualizaEstatusSimpleServiceInDto;
import com.rocket.service.model.DBResponse;
import com.rocket.service.model.DescartarEstatusServiceInDto;
import com.rocket.service.model.RegistroServiceOutDto;
import com.rocket.service.service.CargaService;
import com.rocket.service.service.ConfiguracionService;
import com.rocket.service.service.EstatusService;
import com.rocket.service.service.RegistroService;
import com.rocket.service.service.RolService;
import com.rocket.service.service.UsuarioService;
import com.rocket.service.service.VendorService;
import com.rocket.service.utils.ConfiguracionKey;
import com.rocket.service.utils.TipoEstatus;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class EstatusController {

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

    @Autowired
    ConfiguracionService configuracionService;

    @RequestMapping(value = "/estatus", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> consultaEstatus() {
        List<EstatusDto> estatus = estatusService.obtenerListaEstatus();
        return new ResponseEntity<>((new Gson()).toJson(estatus), HttpStatus.OK);
    }

    @RequestMapping(value = "/estatus", method = RequestMethod.PUT, produces = { "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> actualizaEstatus(
            @RequestBody ActualizaEstatusServiceInDto actualizaEstatusServiceInDto) {
        RegistryDto registro = new RegistryDto();
        registro = registroService.buscarPorOrderKey(new ObjectId(actualizaEstatusServiceInDto.getOrderKey()));

        EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());

        EstatusLogDto estatusLog = new EstatusLogDto();
        estatusLog.setEstatusAnterior(actual.getId());
        estatusLog.setEstatusActual(actualizaEstatusServiceInDto.getEstatusDto().getId());
        estatusLog.setFechaActualizacion(new Date());
        estatusLog.setUsuario(actualizaEstatusServiceInDto.getUser());

        List<EstatusLogDto> estatusLogList = registro.getEstatusLog();

        if (estatusLogList == null)
            estatusLogList = new ArrayList<>();

        estatusLogList.add(estatusLog);

        registro.setEstatusLog(estatusLogList);

        registro.setIdEstatus(actualizaEstatusServiceInDto.getEstatusDto().getId());

        if (registro.getScheduled() != null)
            registro.getScheduled().setIdCourier(actualizaEstatusServiceInDto.getCourier());

        RegistryDto response = registroService.guardar(registro);

        return new ResponseEntity<>((new Gson()).toJson(response), HttpStatus.OK);
    }

    public @ResponseBody ResponseEntity<String> actualizaEstatusRegistroMultipleOld(
            @RequestBody ActualizaEstatusOrderMultipleServiceInDto actualizaEstatusOrderMultipleServiceInDto) {

        List<RegistryDto> registros = new ArrayList<>();
        List<RegistryDto> responses = new ArrayList<>();

        actualizaEstatusOrderMultipleServiceInDto.getOrderKey().forEach(orderKey -> {

            RegistryDto registro = new RegistryDto();
            registro = registroService.buscarPorOrderKey(new ObjectId(orderKey));

            registros.add(registro);

            EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());

            EstatusLogDto estatusLog = new EstatusLogDto();
            estatusLog.setEstatusAnterior(actual.getId());
            estatusLog.setEstatusActual(actualizaEstatusOrderMultipleServiceInDto.getEstatusId());
            estatusLog.setFechaActualizacion(new Date());
            estatusLog.setUsuario(actualizaEstatusOrderMultipleServiceInDto.getUser());

            List<EstatusLogDto> estatusLogList = registro.getEstatusLog();

            if (estatusLogList == null)
                estatusLogList = new ArrayList<>();

            estatusLogList.add(estatusLog);

            registro.setEstatusLog(estatusLogList);

            registro.setIdEstatus(actualizaEstatusOrderMultipleServiceInDto.getEstatusId());

            ScheduledDto scheduled = new ScheduledDto();

            scheduled.setScheduledDate(new Date());
            scheduled.setIdCourier(actualizaEstatusOrderMultipleServiceInDto.getCourier());

            registro.setScheduled(scheduled);

            RegistryDto response = registroService.guardar(registro);
            responses.add(response);
        });

        return new ResponseEntity<>((new Gson()).toJson(responses), HttpStatus.OK);
    }

    @RequestMapping(value = "/estatus/multiple", method = RequestMethod.PUT, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> actualizaEstatusRegistroMultiple(
            @RequestBody ActualizaEstatusOrderMultipleServiceInDto actualizaEstatusOrderMultipleServiceInDto) {

        List<RegistryDto> registros = new ArrayList<>();
        List<RegistryDto> responses = new ArrayList<>();

        List<Map<String, String>> insertResponses = new ArrayList<>();

        Map<String, Object> resultMap = new HashMap<>();
        EstatusDto estatusToValidate = estatusService
                .obtenerEstatusPorId(actualizaEstatusOrderMultipleServiceInDto.getEstatusId());

        UserDto courier = usuarioService.consulta(actualizaEstatusOrderMultipleServiceInDto.getCourier()).get(0);

        actualizaEstatusOrderMultipleServiceInDto.getOrderKey().forEach(orderKey -> {

            RegistryDto registro = new RegistryDto();
            registro = registroService.buscarPorOrderKey(new ObjectId(orderKey));

            registros.add(registro);

            EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());

            RegistryDto response = new RegistryDto();
            if (actual.getSiguiente() == actualizaEstatusOrderMultipleServiceInDto.getEstatusId()) {

                if (courier.getUser().equals(registro.getScheduled().getIdCourier())) {
                    EstatusLogDto estatusLog = new EstatusLogDto();
                    estatusLog.setEstatusAnterior(actual.getId());
                    estatusLog.setEstatusActual(actualizaEstatusOrderMultipleServiceInDto.getEstatusId());
                    estatusLog.setFechaActualizacion(new Date());
                    estatusLog.setUsuario(actualizaEstatusOrderMultipleServiceInDto.getUser());

                    List<EstatusLogDto> estatusLogList = registro.getEstatusLog();

                    if (estatusLogList == null)
                        estatusLogList = new ArrayList<>();

                    estatusLogList.add(estatusLog);

                    registro.setEstatusLog(estatusLogList);

                    registro.setIdEstatus(actualizaEstatusOrderMultipleServiceInDto.getEstatusId());

                    ScheduledDto scheduled = new ScheduledDto();

                    scheduled.setScheduledDate(new Date());
                    scheduled.setIdCourier(actualizaEstatusOrderMultipleServiceInDto.getCourier());

                    registro.setScheduled(scheduled);

                    response = registroService.guardar(registro);
                    responses.add(response);

                    Map<String, String> okResponse = new HashMap<>();
                    String message = "El registro [" + orderKey
                            + "] cambio al estatus <" + estatusToValidate.getDesc() + "> correctamente";
                    okResponse.put("response", "success");
                    okResponse.put("message", message);

                    insertResponses.add(okResponse);
                } else {

                    Map<String, String> badResponse = new HashMap<>();
                    String message = "El registro [" + orderKey
                            + "] no puede cambiar al estatus <" + estatusToValidate.getDesc() + ">";
                    badResponse.put("response", "error");
                    badResponse.put("message", message);

                    insertResponses.add(badResponse);
                }

            } else {

                Map<String, String> badResponse = new HashMap<>();
                String message = "El registro [" + orderKey
                        + "] no puede cambiar al estatus <" + estatusToValidate.getDesc() + ">";
                badResponse.put("response", "error");
                badResponse.put("message", message);

                insertResponses.add(badResponse);
            }

        });

        resultMap.put("response", insertResponses);
        resultMap.put("data", responses);

        return new ResponseEntity<>((new Gson()).toJson(resultMap), HttpStatus.OK);
    }

    @RequestMapping(value = "/estatus/multiple/entrega", method = RequestMethod.PUT, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> actualizaEstatusRegistroMultipleEnEntrega(
            @RequestBody ActualizaEstatusEntregaMultipleOrdersServiceInDto actualizaEstatusEntregaMultipleOrdersServiceInDto) {

        List<RegistryDto> registros = new ArrayList<>();
        List<RegistryDto> responses = new ArrayList<>();

        List<Map<String, String>> insertResponses = new ArrayList<>();

        Map<String, Object> resultMap = new HashMap<>();

        ConfiguracionDto configuracionEstatusAsignadoAlCourier = configuracionService
                .obtenerConfiguracion(ConfiguracionKey.ASIGNADO_AL_COURIER);

        ConfiguracionDto configuracionEstatusEnCursoDeEntrega = configuracionService
                .obtenerConfiguracion(ConfiguracionKey.EN_CURSO_DE_ENTREGA);

        EstatusDto estatusEnCursoDeEntrega = estatusService
                .obtenerEstatusPorId(configuracionEstatusEnCursoDeEntrega.getIntValue());

        actualizaEstatusEntregaMultipleOrdersServiceInDto.getOrderKey().forEach(orderKey -> {

            RegistryDto registro = new RegistryDto();
            registro = registroService.buscarPorOrderKey(new ObjectId(orderKey));

            registros.add(registro);

            EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());

            if (actual.getSiguiente() == configuracionEstatusAsignadoAlCourier.getIntValue()) {

                EstatusLogDto estatusLog = new EstatusLogDto();
                estatusLog.setEstatusAnterior(actual.getId());
                estatusLog.setEstatusActual(actual.getSiguiente());
                estatusLog.setFechaActualizacion(new Date());
                estatusLog.setUsuario(actualizaEstatusEntregaMultipleOrdersServiceInDto.getUser());

                List<EstatusLogDto> estatusLogList = registro.getEstatusLog();

                if (estatusLogList == null)
                    estatusLogList = new ArrayList<>();

                estatusLogList.add(estatusLog);

                registro.setEstatusLog(estatusLogList);

                registro.setIdEstatus(actual.getSiguiente());

                if (registro.getScheduled() != null)
                    registro.getScheduled().setIdCourier(actualizaEstatusEntregaMultipleOrdersServiceInDto.getCourier());

                registro = registroService.guardar(registro);

                estatusLog = new EstatusLogDto();
                estatusLog.setEstatusAnterior(configuracionEstatusAsignadoAlCourier.getIntValue());
                estatusLog.setEstatusActual(configuracionEstatusEnCursoDeEntrega.getIntValue());
                estatusLog.setFechaActualizacion(new Date());
                estatusLog.setUsuario(actualizaEstatusEntregaMultipleOrdersServiceInDto.getUser());

                estatusLogList = registro.getEstatusLog();

                if (estatusLogList == null)
                    estatusLogList = new ArrayList<>();

                estatusLogList.add(estatusLog);

                registro.setEstatusLog(estatusLogList);

                registro.setIdEstatus(configuracionEstatusEnCursoDeEntrega.getIntValue());

                RegistryDto response = registroService.guardar(registro);

                responses.add(response);

                Map<String, String> okResponse = new HashMap<>();
                String message = "El registro [" + orderKey
                        + "] cambio al estatus <" + estatusEnCursoDeEntrega.getDesc() + "> correctamente";
                okResponse.put("response", "success");
                okResponse.put("message", message);

                insertResponses.add(okResponse);

            } else {

                Map<String, String> badResponse = new HashMap<>();
                String message = "El registro [" + orderKey
                        + "] no puede cambiar al estatus <" + estatusEnCursoDeEntrega.getDesc() + ">";
                badResponse.put("response", "error");
                badResponse.put("message", message);

                insertResponses.add(badResponse);
            }

        });

        resultMap.put("response", insertResponses);
        resultMap.put("data", responses);

        return new ResponseEntity<>((new Gson()).toJson(resultMap), HttpStatus.OK);
    }

    @RequestMapping(value = "/estatus/simple", method = RequestMethod.PUT, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> actualizaEstatusSimple(
            @RequestBody ActualizaEstatusSimpleServiceInDto actualizaEstatusSimpleServiceInDto) {
        RegistryDto registro = new RegistryDto();
        registro = registroService.buscarPorOrderKey(new ObjectId(actualizaEstatusSimpleServiceInDto.getOrderKey()));

        EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());

        EstatusLogDto estatusLog = new EstatusLogDto();
        estatusLog.setEstatusAnterior(actual.getId());
        estatusLog.setEstatusActual(actualizaEstatusSimpleServiceInDto.getIdEstatus());
        estatusLog.setFechaActualizacion(new Date());
        estatusLog.setUsuario(actualizaEstatusSimpleServiceInDto.getUsuario());

        List<EstatusLogDto> estatusLogList = registro.getEstatusLog();

        if (estatusLogList == null)
            estatusLogList = new ArrayList<>();

        estatusLogList.add(estatusLog);

        registro.setEstatusLog(estatusLogList);

        registro.setIdEstatus(actualizaEstatusSimpleServiceInDto.getIdEstatus());

        EstatusDto nuevoEstatus = estatusService.obtenerEstatusPorId(actualizaEstatusSimpleServiceInDto.getIdEstatus());

        EstatusDto aux = estatusService.obtenerEstatusPorId(actual.getSiguiente());

        if (nuevoEstatus.getTipo().equals(TipoEstatus.FINAL.getDescripcion())
                || aux.getTipo().equals(TipoEstatus.FINAL.getDescripcion())) {
            if (actualizaEstatusSimpleServiceInDto.getDeliveryComment() != null
                    || !actualizaEstatusSimpleServiceInDto.getDeliveryComment().isEmpty())
                registro.setDeliveryComment(actualizaEstatusSimpleServiceInDto.getDeliveryComment());
        }

        if (nuevoEstatus.getTipo().equals(TipoEstatus.EXCEPCION.getDescripcion())
                || aux.getTipo().equals(TipoEstatus.EXCEPCION.getDescripcion())) {
            if (actualizaEstatusSimpleServiceInDto.getDeliveryException() != null)
                registro.setDeliveryException(actualizaEstatusSimpleServiceInDto.getDeliveryException());
        }

        RegistryDto response = registroService.guardar(registro);

        return new ResponseEntity<>((new Gson()).toJson(response), HttpStatus.OK);
    }

    @RequestMapping(value = "/estatus/list", method = RequestMethod.PUT, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> actualizaEstatusList(
            @RequestBody List<ActualizaEstatusServiceInDto> actualizaEstatusServiceInDtoList) {

        List<RegistroServiceOutDto> response = new ArrayList<>();
        actualizaEstatusServiceInDtoList.forEach(actualizaEstatusServiceInDto -> {
            RegistryDto registro = new RegistryDto();
            registro = registroService.buscarPorOrderKey(new ObjectId(actualizaEstatusServiceInDto.getOrderKey()));

            registro.setDeliveryComment(actualizaEstatusServiceInDto.getComment());
            EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());

            EstatusLogDto estatusLog = new EstatusLogDto();
            estatusLog.setEstatusAnterior(actual.getId());
            estatusLog.setEstatusActual(actualizaEstatusServiceInDto.getEstatusDto().getId());
            estatusLog.setFechaActualizacion(new Date());
            estatusLog.setUsuario(actualizaEstatusServiceInDto.getUser());

            List<EstatusLogDto> estatusLogList = registro.getEstatusLog();

            if (estatusLogList == null)
                estatusLogList = new ArrayList<>();

            estatusLogList.add(estatusLog);

            registro.setEstatusLog(estatusLogList);

            registro.setIdEstatus(actualizaEstatusServiceInDto.getEstatusDto().getId());

            if (registro.getScheduled() != null && actualizaEstatusServiceInDto.getCourier() != null)
                registro.getScheduled().setIdCourier(actualizaEstatusServiceInDto.getCourier());

            try {
                RegistryDto r = registroService.guardar(registro);
                LoadDto carga = cargaService.obtenerCargaPorId(r.getIdCarga());
                RegistroServiceOutDto res = RegistroMapper.mapRegistroCargaToRegistroOut(r, carga, usuarioService,
                        vendorService, estatusService);
                response.add(res);
            } catch (Exception e) {
                log.error(e.toString());
            }
        });

        return new ResponseEntity<>((new Gson()).toJson(response), HttpStatus.OK);
    }

    @RequestMapping(value = "/estatus/discard", method = RequestMethod.PUT, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> descartarEstatusList(
            @RequestBody List<DescartarEstatusServiceInDto> descartarEstatusServiceInDtoList) {

        List<RegistroServiceOutDto> response = new ArrayList<>();
        descartarEstatusServiceInDtoList.forEach(descartarEstatusServiceInDto -> {
            RegistryDto registro = new RegistryDto();
            registro = registroService.buscarPorOrderKey(new ObjectId(descartarEstatusServiceInDto.getOrderKey()));

            EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());

            EstatusLogDto estatusLog = new EstatusLogDto();
            estatusLog.setEstatusAnterior(actual.getId());
            estatusLog.setEstatusActual(descartarEstatusServiceInDto.getIdEstatus());
            estatusLog.setFechaActualizacion(new Date());
            estatusLog.setUsuario(descartarEstatusServiceInDto.getUser());

            List<EstatusLogDto> estatusLogList = registro.getEstatusLog();

            if (estatusLogList == null)
                estatusLogList = new ArrayList<>();

            estatusLogList.add(estatusLog);

            registro.setEstatusLog(estatusLogList);

            registro.setIdEstatus(descartarEstatusServiceInDto.getIdEstatus());

            try {
                RegistryDto r = registroService.guardar(registro);
                LoadDto carga = cargaService.obtenerCargaPorId(r.getIdCarga());
                RegistroServiceOutDto res = RegistroMapper.mapRegistroCargaToRegistroOut(r, carga, usuarioService,
                        vendorService, estatusService);
                response.add(res);
            } catch (Exception e) {
                log.error(e.toString());
            }
        });

        return new ResponseEntity<>((new Gson()).toJson(response), HttpStatus.OK);
    }

    @RequestMapping(value = "/estatus/{username}", method = RequestMethod.GET, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> consultaEstatusPorRol(@PathVariable String username) {

        List<UserDto> usuarioList = usuarioService.consulta(username);
        if (usuarioList.isEmpty())
            return new ResponseEntity<>("Usuario: " + username + " no encontrado.", HttpStatus.BAD_REQUEST);

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

        return new ResponseEntity<>((new Gson()).toJson(estatusListResult), HttpStatus.OK);
    }

    @RequestMapping(value = "/estatus-change/{username}", method = RequestMethod.GET, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> consultaEstatusChangePorRol(@PathVariable String username) {

        List<UserDto> usuarioList = usuarioService.consulta(username);
        if (usuarioList.isEmpty())
            return new ResponseEntity<>("Usuario: " + username + " no encontrado.", HttpStatus.BAD_REQUEST);

        UserDto usuario = usuarioList.get(0);

        List<RolDto> roles = rolService.consultaRol(usuario.getRol());

        if (roles.isEmpty())
            return new ResponseEntity<>("Rol: " + usuario.getRol() + " no encontrado.", HttpStatus.BAD_REQUEST);

        RolDto rol = roles.get(0);

        List<EstatusDto> estatusList = estatusService.obtenerListaEstatus();

        List<EstatusDto> estatusListResult = new ArrayList<EstatusDto>();

        for (EstatusDto estatus : estatusList) {
            if (rol.getStatusChange().contains(estatus.getId()))
                estatusListResult.add(estatus);
        }

        return new ResponseEntity<>((new Gson()).toJson(estatusListResult), HttpStatus.OK);
    }

    @RequestMapping(value = "/estatus/{idEstatus}/siguiente", method = RequestMethod.GET, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> consultaEstatusSiguiente(@PathVariable Integer idEstatus) {

        EstatusDto actual = estatusService.obtenerEstatusPorId(idEstatus);
        EstatusDto siguiente = estatusService.obtenerEstatusSiguiente(actual);

        if (siguiente == null) {
            DBResponse response = new DBResponse();

            response.setResponse(false);
            response.setResponseMessage("El estatus no puede ser final o una excepción");
            return new ResponseEntity<>((new Gson()).toJson(response), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>((new Gson()).toJson(siguiente), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/estatus/{idEstatus}/siguiente-exception", method = RequestMethod.GET, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> consultaEstatusSiguienteException(@PathVariable Integer idEstatus) {

        EstatusDto actual = estatusService.obtenerEstatusPorId(idEstatus);
        EstatusDto siguiente = estatusService.obtenerEstatusSiguienteExcepcion(actual);

        return new ResponseEntity<>((new Gson()).toJson(siguiente), HttpStatus.OK);

    }

    @RequestMapping(value = "/estatus/{idEstatus}/siguientes", method = RequestMethod.GET, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> consultaEstatusSiguientes(@PathVariable Integer idEstatus) {

        EstatusDto actual = estatusService.obtenerEstatusPorId(idEstatus);
        List<EstatusDto> siguientes = estatusService.obtenerEstatusSiguientes(actual);

        if (siguientes.isEmpty()) {
            DBResponse response = new DBResponse();

            response.setResponse(false);
            response.setResponseMessage("El estatus no puede ser final");
            return new ResponseEntity<>((new Gson()).toJson(response), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>((new Gson()).toJson(siguientes), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/estatus/{idEstatus}/detalle", method = RequestMethod.GET, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> consultaDetalleEstatus(@PathVariable Integer idEstatus) {

        EstatusDto actual = estatusService.obtenerEstatusPorId(idEstatus);

        if (actual == null) {
            DBResponse response = new DBResponse();

            response.setResponse(false);
            response.setResponseMessage("No se encontro el estatus");
            return new ResponseEntity<>((new Gson()).toJson(response), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>((new Gson()).toJson(actual), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/estatus/valida/{idEstatus}/{courier}", method = RequestMethod.GET, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> validaEstatus(@PathVariable Integer idEstatus,
            @PathVariable String courier) {

        List<UserDto> busqueda = usuarioService.consulta(courier);

        if (busqueda == null || busqueda.isEmpty()) {
            DBResponse response = new DBResponse();

            response.setResponse(false);
            response.setResponseMessage("No se encontro el courier");
            return new ResponseEntity<>((new Gson()).toJson(response), HttpStatus.BAD_REQUEST);
        }

        UserDto courierDto = busqueda.get(0);

        List<RolDto> busquedaRol = rolService.consultaRol(courierDto.getRol());

        if (busquedaRol == null || busquedaRol.isEmpty()) {
            DBResponse response = new DBResponse();

            response.setResponse(false);
            response.setResponseMessage("No se encontro el rol");
            return new ResponseEntity<>((new Gson()).toJson(response), HttpStatus.BAD_REQUEST);
        }

        RolDto rolDto = busquedaRol.get(0);

        Boolean result = rolDto.getStatusChange().contains(idEstatus);

        return new ResponseEntity<>((new Gson()).toJson(result), HttpStatus.OK);
    }

}
