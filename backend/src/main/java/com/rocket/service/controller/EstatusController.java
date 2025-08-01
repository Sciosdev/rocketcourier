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
import com.rocket.service.entity.VendorDto;
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
import com.rocket.service.service.ShopifySyncService;
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

    @Autowired
    ShopifySyncService shopifySyncService;

    private VendorDto getVendor(RegistryDto registro) {
        LoadDto carga = cargaService.obtenerCargaPorId(registro.getIdCarga());
        List<UserDto> users = usuarioService.consulta(carga.getIdVendor());
        if (users == null || users.isEmpty()) {
            return null;
        }
        UserDto vendedor = users.get(0);
        return vendorService.obtenerTiendaPorId(vendedor.getTienda() != null ? vendedor.getTienda().longValue() : null);
    }

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

        if (response.getOrder() != null && response.getOrder().isShopifyOrder()) {
            VendorDto vendor = getVendor(response);
            if (vendor != null) {
                shopifySyncService.createFulfillment(vendor, response.getOrder().getId());
                shopifySyncService.postFulfillmentEvent(vendor, response.getOrder().getId(), "0", estatusService.obtenerEstatusPorId(response.getIdEstatus()).getDesc());
            }
        }

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
            if (response.getOrder() != null && response.getOrder().isShopifyOrder()) {
                VendorDto vendor = getVendor(response);
                if (vendor != null) {
                    shopifySyncService.createFulfillment(vendor, response.getOrder().getId());
                    shopifySyncService.postFulfillmentEvent(vendor, response.getOrder().getId(), "0", estatusService.obtenerEstatusPorId(response.getIdEstatus()).getDesc());
                }
            }
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
                    if (response.getOrder() != null && response.getOrder().isShopifyOrder()) {
                        VendorDto vendor = getVendor(response);
                        if (vendor != null) {
                            shopifySyncService.createFulfillment(vendor, response.getOrder().getId());
                            shopifySyncService.postFulfillmentEvent(vendor, response.getOrder().getId(), "0", estatusService.obtenerEstatusPorId(response.getIdEstatus()).getDesc());
                        }
                    }

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
                if (registro.getOrder() != null && registro.getOrder().isShopifyOrder()) {
                    VendorDto vendor = getVendor(registro);
                    if (vendor != null) {
                        shopifySyncService.createFulfillment(vendor, registro.getOrder().getId());
                        shopifySyncService.postFulfillmentEvent(vendor, registro.getOrder().getId(), "0", estatusService.obtenerEstatusPorId(registro.getIdEstatus()).getDesc());
                    }
                }

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
                if (response.getOrder() != null && response.getOrder().isShopifyOrder()) {
                    VendorDto vendor = getVendor(response);
                    if (vendor != null) {
                        shopifySyncService.createFulfillment(vendor, response.getOrder().getId());
                        shopifySyncService.postFulfillmentEvent(vendor, response.getOrder().getId(), "0", estatusService.obtenerEstatusPorId(response.getIdEstatus()).getDesc());
                    }
                }

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

        if (response.getOrder() != null && response.getOrder().isShopifyOrder()) {
            VendorDto vendor = getVendor(response);
            if (vendor != null) {
                shopifySyncService.createFulfillment(vendor, response.getOrder().getId());
                shopifySyncService.postFulfillmentEvent(vendor, response.getOrder().getId(), "0", estatusService.obtenerEstatusPorId(response.getIdEstatus()).getDesc());
            }
        }

        return new ResponseEntity<>((new Gson()).toJson(response), HttpStatus.OK);
    }

    @RequestMapping(value = "/estatus/list", method = RequestMethod.PUT, produces = {
            "application/json;charset=UTF-8" })
    public @ResponseBody ResponseEntity<String> actualizaEstatusList(
            @RequestBody List<ActualizaEstatusServiceInDto> actualizaEstatusServiceInDtoList) {

        // IDs de estatus relevantes y URL base de Rocket
        final Integer ID_ESTATUS_AGENDA_ACEPTADA_PENDIENTE_RECOLECCION = 3;
        final Integer ID_ESTATUS_RECOLECTADO = 6;
        final String ROCKET_BASE_URL = "https://main.d3je47rbud1pwk.amplifyapp.com";

        List<RegistroServiceOutDto> responseFramework = new ArrayList<>(); // Renombrado para claridad
        actualizaEstatusServiceInDtoList.forEach(dto -> {
            RegistryDto registro = registroService.buscarPorOrderKey(new ObjectId(dto.getOrderKey()));

            if (registro == null) {
                log.warn("No se encontró registro para orderKey {} en actualizaEstatusList", dto.getOrderKey());
                // Considerar cómo manejar la respuesta para este caso si es necesario.
                // Por ahora, simplemente se omite de la respuesta.
                return; // Salta a la siguiente iteración del forEach
            }
            if (registro.getOrder() == null) {
                log.warn("OrderDto es nulo para el registro {} (orderKey {}) en actualizaEstatusList", registro.getId(), dto.getOrderKey());
                return;
            }

            registro.setDeliveryComment(dto.getComment());
            EstatusDto estatusActual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());

            if (estatusActual == null) {
                log.warn("No se encontró estatus actual para el registro {} (orderKey {}) con idEstatus {}", registro.getId(), dto.getOrderKey(), registro.getIdEstatus());
                return;
            }

            Integer idEstatusOriginal = estatusActual.getId();
            Integer idEstatusNuevo = dto.getEstatusDto().getId();

            EstatusLogDto estatusLog = new EstatusLogDto();
            estatusLog.setEstatusAnterior(idEstatusOriginal);
            estatusLog.setEstatusActual(idEstatusNuevo);
            estatusLog.setFechaActualizacion(new Date());
            estatusLog.setUsuario(dto.getUser());

            List<EstatusLogDto> estatusLogList = registro.getEstatusLog();
            if (estatusLogList == null) {
                estatusLogList = new ArrayList<>();
            }
            estatusLogList.add(estatusLog);
            registro.setEstatusLog(estatusLogList);

            registro.setIdEstatus(idEstatusNuevo);

            if (registro.getScheduled() != null && dto.getCourier() != null) {
                registro.getScheduled().setIdCourier(dto.getCourier());
            }

            // ---> INICIO DE LA LÓGICA DE SHOPIFY (PASO 2) <---
            if (registro.getOrder().isShopifyOrder() &&
                ID_ESTATUS_AGENDA_ACEPTADA_PENDIENTE_RECOLECCION.equals(idEstatusOriginal) &&
                ID_ESTATUS_RECOLECTADO.equals(idEstatusNuevo)) {

                VendorDto vendor = getVendor(registro); // Usa el método getVendor de EstatusController
                if (vendor != null && vendor.getShopifyStoreUrl() != null && vendor.getShopifyAccessToken() != null) {
                    log.info("Pedido Shopify {} (orderKey {}) cambiando de estado {} a {}. Intentando crear fulfillment con tracking.",
                             registro.getOrder().getId(), dto.getOrderKey(), idEstatusOriginal, idEstatusNuevo);

                    String shopifyFulfillmentId = shopifySyncService.createFulfillmentWithTracking(vendor, registro, ROCKET_BASE_URL);

                    if (shopifyFulfillmentId != null && !shopifyFulfillmentId.isEmpty()) {
                        registro.getOrder().setShopifyFulfillmentId(shopifyFulfillmentId);
                        log.info("Fulfillment de Shopify creado con ID {} para orderKey {}", shopifyFulfillmentId, dto.getOrderKey());
                    } else {
                        log.warn("No se pudo crear/obtener el fulfillment ID de Shopify para orderKey {}", dto.getOrderKey());
                    }
                } else {
                    log.warn("No se puede sincronizar Paso 2 con Shopify para orderKey {}: Vendor o configuración de Shopify faltante.", dto.getOrderKey());
                }
            }
            // ---> FIN DE LA LÓGICA DE SHOPIFY (PASO 2) <---


            // ---> INICIO DE LA LÓGICA DE EVENTOS DE FULFILLMENT DE SHOPIFY <---
            if (registro.getOrder().isShopifyOrder()) {
                String shopifyFulfillmentId = registro.getOrder().getShopifyFulfillmentId();
                if (shopifyFulfillmentId != null && !shopifyFulfillmentId.isEmpty()) {
                    VendorDto vendor = getVendor(registro);
                    if (vendor != null) {
                        String shopifyOrderId = registro.getOrder().getId();

                        // IDs de estatus para los eventos
                        final Integer ID_ESTATUS_ENTREGADO_EN_BODEGA = 7;
                        final Integer ID_ESTATUS_ASIGNADO_AL_COURIER = 8;
                        final Integer ID_ESTATUS_EN_CURSO_DE_ENTREGA = 9;
                        final Integer ID_ESTATUS_ENTREGA_FALLIDA = 10;
                        final Integer ID_ESTATUS_ENTREGADO_AL_USUARIO_FINAL = 11;

                        if (ID_ESTATUS_RECOLECTADO.equals(idEstatusOriginal) && ID_ESTATUS_ENTREGADO_EN_BODEGA.equals(idEstatusNuevo)) {
                            shopifySyncService.postFulfillmentEvent(vendor, registro.getOrder(), shopifyFulfillmentId, "ready_for_pickup", "Paquete listo para recogida en bodega");
                        } else if (ID_ESTATUS_ENTREGADO_EN_BODEGA.equals(idEstatusOriginal) && ID_ESTATUS_ASIGNADO_AL_COURIER.equals(idEstatusNuevo)) {
                            shopifySyncService.postFulfillmentEvent(vendor, registro.getOrder(), shopifyFulfillmentId, "in_transit", "Courier asignado y paquete en tránsito");
                        } else if (ID_ESTATUS_ASIGNADO_AL_COURIER.equals(idEstatusOriginal) && ID_ESTATUS_EN_CURSO_DE_ENTREGA.equals(idEstatusNuevo)) {
                            shopifySyncService.postFulfillmentEvent(vendor, registro.getOrder(), shopifyFulfillmentId, "out_for_delivery", "Paquete en reparto para entrega al cliente");
                        } else if (ID_ESTATUS_EN_CURSO_DE_ENTREGA.equals(idEstatusOriginal) && ID_ESTATUS_ENTREGADO_AL_USUARIO_FINAL.equals(idEstatusNuevo)) {
                            shopifySyncService.postFulfillmentEvent(vendor, registro.getOrder(), shopifyFulfillmentId, "delivered", "Pedido entregado al cliente");
                        } else if (ID_ESTATUS_EN_CURSO_DE_ENTREGA.equals(idEstatusOriginal) && ID_ESTATUS_ENTREGA_FALLIDA.equals(idEstatusNuevo)) {
                            shopifySyncService.postFulfillmentEvent(vendor, registro.getOrder(), shopifyFulfillmentId, "attempted_delivery", "Entrega fallida, paquete en retorno a bodega");
                        }
                    }
                } else {
                    // Log si se intenta enviar un evento pero no hay fulfillmentId.
                    // Esto puede ocurrir si el Paso 2 falló o si el flujo de estados no es el esperado.
                    log.warn("Se intentó enviar un evento de fulfillment para orderKey {}, pero no se encontró shopifyFulfillmentId.", dto.getOrderKey());
                }
            }
            // ---> FIN DE LA LÓGICA DE EVENTOS DE FULFILLMENT DE SHOPIFY <---


            // ---> INICIO LÓGICA DE SHOPIFY EXISTENTE (REVISAR SI APLICA O ES REDUNDANTE/CONFLICTIVA) <---
            // Esta lógica ya estaba en el método. Es importante revisar si interfiere
            // con la nueva lógica del Paso 2 o si es para un flujo diferente.
            // Si createFulfillment y postFulfillmentEvent son para otros pasos/eventos, deberían tener sus propias condiciones.
            // Por ahora, la dejo comentada para evitar ejecuciones dobles o inesperadas.
            /*
            if (registro.getOrder() != null && registro.getOrder().isShopifyOrder()) {
                VendorDto vendor = getVendor(registro);
                if (vendor != null) {
                    // ¿En qué condiciones se debe llamar a esto? ¿Es diferente del Paso 2?
                    // shopifySyncService.createFulfillment(vendor, registro.getOrder().getId());
                    // shopifySyncService.postFulfillmentEvent(vendor, registro.getOrder().getId(), "0", estatusService.obtenerEstatusPorId(registro.getIdEstatus()).getDesc());
                    log.info("Lógica de Shopify existente en actualizaEstatusList para orderKey {} (Shopify Order ID {}). Revisar si aún es necesaria o si causa conflicto.",
                             dto.getOrderKey(), registro.getOrder().getId());
                }
            }
            */
            // ---> FIN LÓGICA DE SHOPIFY EXISTENTE <---


            try {
                RegistryDto r = registroService.guardar(registro); // Guardar el registro con el posible shopifyFulfillmentId
                LoadDto carga = cargaService.obtenerCargaPorId(r.getIdCarga());
                RegistroServiceOutDto res = RegistroMapper.mapRegistroCargaToRegistroOut(r, carga, usuarioService,
                        vendorService, estatusService);
                responseFramework.add(res);
            } catch (Exception e) {
                log.error("Error al guardar o mapear el registro para orderKey {}: {}", dto.getOrderKey(), e.toString(), e);
            }
        });

        return new ResponseEntity<>((new Gson()).toJson(responseFramework), HttpStatus.OK);
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
                if (r.getOrder() != null && r.getOrder().isShopifyOrder()) {
                    VendorDto vendor = getVendor(r);
                    if (vendor != null) {
                        shopifySyncService.createFulfillment(vendor, r.getOrder().getId());
                        shopifySyncService.postFulfillmentEvent(vendor, r.getOrder().getId(), "0", estatusService.obtenerEstatusPorId(r.getIdEstatus()).getDesc());
                    }
                }
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
