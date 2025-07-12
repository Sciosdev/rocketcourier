package com.rocket.service.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
// Ya no necesitamos JsonSyntaxException aquí si Jackson maneja la deserialización principal
import com.rocket.service.entity.EstatusDto;
import com.rocket.service.entity.EstatusLogDto;
import com.rocket.service.entity.LoadDto;
import com.rocket.service.entity.RegistryDto;
import com.rocket.service.entity.ScheduledDto;
import com.rocket.service.entity.UserDto;
import com.rocket.service.entity.VendorDto;
import com.rocket.service.mapper.EstatusMapper;
import com.rocket.service.mapper.RegistroMapper;
import com.rocket.service.model.DBResponse;
import com.rocket.service.model.EstatusLogDataDto;
import com.rocket.service.model.EstatusLogServiceDto;
import com.rocket.service.model.HistoricoDto;
import com.rocket.service.model.RegistroServiceInDto;
import com.rocket.service.model.RegistroServiceOutDto;
import com.rocket.service.model.ScheduleServiceInDto;
import com.rocket.service.model.ShopifyFulfillmentData;
import com.rocket.service.exception.ShopifyApiException;
import com.rocket.service.service.CargaService;
import com.rocket.service.service.EstatusService;
import com.rocket.service.service.RegistroService;
import com.rocket.service.service.RolService;
import com.rocket.service.service.SequenceGeneratorService;
import com.rocket.service.service.UsuarioService;
import com.rocket.service.service.VendorService;
import com.rocket.service.service.ShopifySyncService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableTransactionManagement
@Slf4j
public class TransaccionesRestController {

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

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @RequestMapping(value = "/registro", method = RequestMethod.POST, produces = {
            "application/json;charset=UTF-8" }, consumes = { "application/json" })
    public @ResponseBody ResponseEntity<LoadDto> guardarRegistros(@RequestBody RegistroServiceInDto regis) {
        Gson gsonForLog = new GsonBuilder().setPrettyPrinting().create();
        log.info("Endpoint /registro POST recibido. Payload: {}", gsonForLog.toJson(regis));

        LoadDto regisCarga = new LoadDto();
        List<String> errores = new ArrayList<>();
        EstatusDto estatusInicial = estatusService.obtenerEstatusInicial();
        Long idCarga = null;

        AtomicInteger exitoso = new AtomicInteger(0);
        AtomicInteger fallido = new AtomicInteger(0);
        AtomicInteger omitido = new AtomicInteger(0);

        regisCarga.setTipoCarga(regis.getTipoCarga());
        regisCarga.setIdVendor(regis.getIdVendor());
        regisCarga.setUploadDate(new Date());

        if (regis.getRegistro() == null || regis.getRegistro().isEmpty()) {
            log.info("La lista de registros en el payload está vacía o es nula.");
            regisCarga.setRespuesta("No se enviaron registros para procesar.");
            return ResponseEntity.ok(regisCarga);
        }

        regisCarga.setIdCarga(sequenceGeneratorService.generateSequence(LoadDto.SEQUENCE_NAME));
        // Guardar la carga inicial ANTES del bucle para tener un idCarga
        LoadDto insertedCarga = cargaService.guardarCarga(regisCarga);
        idCarga = insertedCarga.getIdCarga();
        regisCarga.setIdCarga(idCarga);

        final Long finalIdCarga = idCarga;

        if (regis.getTipoCarga() == 0) { // API de Shopify
            regis.getRegistro().forEach(registro -> {
                if (registro.getOrder() == null) {
                    log.warn("Se encontró un RegistryDto con OrderDto nulo para idCarga: {}", finalIdCarga);
                    errores.add("Registro inválido: datos del pedido faltantes.");
                    fallido.incrementAndGet();
                    return;
                }

                String shopifyOrderId = registro.getOrder().getId();
                String orderName = registro.getOrder().getName() != null ? registro.getOrder().getName() : "ID " + shopifyOrderId;

                if (registro.getOrder().isShopifyOrder() && shopifyOrderId != null && !shopifyOrderId.isEmpty()) {
                    if (registroService.existePedidoShopify(shopifyOrderId, regis.getIdVendor())) {
                        log.info("Pedido de Shopify {} ({}) para vendor {} ya existe. Omitiendo.", orderName, shopifyOrderId, regis.getIdVendor());
                        errores.add("Pedido [" + orderName + "] omitido: ya existe.");
                        omitido.incrementAndGet();
                        return;
                    }
                }

                String financialStatus = registro.getOrder().getFinancial_status();
                if (financialStatus != null && financialStatus.equalsIgnoreCase("Paid")) {
                    try {
                        registro.setIdCarga(finalIdCarga);
                        registro.setId(sequenceGeneratorService.generateSequence(RegistryDto.SEQUENCE_NAME));
                        registro.setIdEstatus(estatusInicial.getId());
                        if (registro.getOrder().getOrderKey() == null) {
                            registro.getOrder().setOrderKey(new ObjectId());
                        }

                        String validationError = registroService.validacion(registro);
                        if (validationError == null || validationError.trim().isEmpty()) {
                            EstatusLogDto estatusLog = new EstatusLogDto();
                            estatusLog.setEstatusAnterior(null);
                            estatusLog.setEstatusActual(registro.getIdEstatus());
                            estatusLog.setFechaActualizacion(new Date());
                            estatusLog.setUsuario(regis.getIdVendor());
                            List<EstatusLogDto> estatusLogList = new ArrayList<>();
                            estatusLogList.add(estatusLog);
                            registro.setEstatusLog(estatusLogList);

                            RegistryDto insercion = registroService.guardar(registro);
                            if (insercion != null) {
                                exitoso.incrementAndGet();
                            } else {
                                log.error("Error al guardar registro (insercion nula) para orden: {} con idCarga: {}", orderName, finalIdCarga);
                                errores.add("Error en pedido [" + orderName + "] : Falla desconocida al guardar.");
                                fallido.incrementAndGet();
                            }
                        } else {
                            log.warn("Validación fallida para el pedido: {} con idCarga: {}. Errores: {}", orderName, finalIdCarga, validationError.trim());
                            errores.add("Error en pedido [" + orderName + "] : " + validationError.trim());
                            fallido.incrementAndGet();
                        }
                    } catch (Exception e) {
                        log.error("Excepción procesando pedido: {} con idCarga: {}", orderName, finalIdCarga, e);
                        errores.add("Error en pedido [" + orderName + "] : " + e.getMessage());
                        fallido.incrementAndGet();
                    }
                } else {
                    log.info("Pedido {} omitido (financial_status no es 'Paid' o es nulo: '{}') con idCarga: {}", orderName, financialStatus, finalIdCarga);
                    omitido.incrementAndGet();
                }
            });
        } else { // Lógica para tipoCarga != 0 (CSV)
            regis.getRegistro().forEach(registro -> {
                RegistryDto insercion;
                String rowNum = registro.getRowNumber() != null ? registro.getRowNumber() : "N/A";
                String orderName = (registro.getOrder() != null && registro.getOrder().getName() != null) ? registro.getOrder().getName() : "Fila " + rowNum;
                try {
                    registro.setIdCarga(finalIdCarga);
                    registro.setId(sequenceGeneratorService.generateSequence(RegistryDto.SEQUENCE_NAME));
                    registro.setIdEstatus(estatusInicial.getId());
                    if (registro.getOrder() != null && registro.getOrder().getOrderKey() == null) {
                       registro.getOrder().setOrderKey(new ObjectId());
                    }

                    // Aquí se podría llamar a registroService.validacion(registro) si se desea la misma validación para CSV
                    // String validationErrorCSV = registroService.validacion(registro);
                    // if (validationErrorCSV == null || validationErrorCSV.trim().isEmpty()) { ... }

                    EstatusLogDto estatusLog = new EstatusLogDto();
                    estatusLog.setEstatusAnterior(null);
                    estatusLog.setEstatusActual(registro.getIdEstatus());
                    estatusLog.setFechaActualizacion(new Date());
                    estatusLog.setUsuario(regis.getIdVendor());
                    List<EstatusLogDto> estatusLogList = new ArrayList<>();
                    estatusLogList.add(estatusLog);
                    registro.setEstatusLog(estatusLogList);

                    insercion = registroService.guardar(registro);
                    if (insercion != null) {
                        exitoso.incrementAndGet();
                    } else {
                        fallido.incrementAndGet();
                         errores.add("Error en fila [" + rowNum + "] ("+orderName+"): Falla desconocida al guardar (CSV).");
                    }
                } catch (Exception e) {
                    log.error("Excepción procesando registro CSV para fila [{}], pedido {}: ", rowNum, orderName, e);
                    errores.add("Error en fila [" + rowNum + "] ("+orderName+"): " + e.getMessage());
                    fallido.incrementAndGet();
                }
            });
        }

        regisCarga.setRegistrosExitosos(exitoso.get());
        regisCarga.setRegistrosFallidos(fallido.get());
        regisCarga.setRegistrosOmitidos(omitido.get());

        if (errores.isEmpty()) {
            if (exitoso.get() > 0) {
                regisCarga.setRespuesta("Proceso de carga finalizado.");
            } else if (omitido.get() > 0 && exitoso.get() == 0 && fallido.get() == 0) {
                regisCarga.setRespuesta("Todos los pedidos ya existían o no cumplieron criterios para ser procesados (ej. no pagados).");
            } else if (fallido.get() == 0 && omitido.get() == 0 && exitoso.get() == 0 ) {
                 regisCarga.setRespuesta("No se procesaron registros, revise los logs o los datos de entrada.");
            } else {
                 regisCarga.setRespuesta("Proceso de carga completado sin errores explícitos, pero sin registros exitosos.");
            }
        } else {
            regisCarga.setRespuesta(StringUtils.join(errores, " | "));
        }

        // Actualizar el LoadDto con los contadores finales
        // Solo guardar si hubo alguna actividad o error que valga la pena registrar
        if (exitoso.get() > 0 || fallido.get() > 0 || omitido.get() > 0) {
             LoadDto finalLoad = cargaService.guardarCarga(regisCarga);
             return ResponseEntity.ok(finalLoad);
        } else {
            // Si no se procesó nada (ej. lista vacía inicial o todos los pedidos no aplicables sin error)
            // Devolvemos el regisCarga sin actualizarlo en BD, ya que el 'inserted' inicial podría ser suficiente
            // o incluso podríamos no querer guardar una carga vacía.
            // Para consistencia, si se creó un idCarga, lo actualizamos.
            // Si idCarga es null (porque la lista de entrada estaba vacía), no se guarda.
            if (regisCarga.getIdCarga() != null) {
                return ResponseEntity.ok(cargaService.guardarCarga(regisCarga));
            }
            return ResponseEntity.ok(regisCarga);
        }
    }

	private @ResponseBody ResponseEntity<String> obtenerRegistros(Integer idEstatus, List<String> customer, String courier) {
		List<RegistroServiceOutDto> registros = new ArrayList<RegistroServiceOutDto>();
		Gson gson = new Gson();
		String json = "";
		List<LoadDto> cargas;

		if (customer != null && !customer.isEmpty()) {
            cargas = cargaService.consultaCargaVendedor(customer);
            if (cargas != null) {
                cargas.forEach(carga -> {
                    List<RegistryDto> regisList;
                    regisList = registroService.consultaRegistroCargaEstatus(carga.getIdCarga(), idEstatus, courier);
                    if (regisList != null) {
                        regisList.forEach(registro -> {
                            RegistroServiceOutDto r = RegistroMapper.mapRegistroCargaToRegistroOut(registro, carga,
                                    usuarioService, vendorService, estatusService);
                            registros.add(r);
                        });
                    }
                });
            }
		} else {
			List<RegistryDto> temp = registroService.consultaRegistroPorCourierYEstatus(courier, idEstatus);
            if (temp != null) {
                temp.forEach(registro -> {
                    LoadDto carga = cargaService.obtenerCargaPorId(registro.getIdCarga());
                    if (registro != null && carga != null) {
                        registros.add(RegistroMapper.mapRegistroCargaToRegistroOut(registro, carga, usuarioService,
                                vendorService, estatusService));
                    }
                });
            }
		}

		json = gson.toJson(registros);
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@RequestMapping(value = "/repartidor", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> obtenerRepartidor() {
        List<UserDto> users;
		Gson gson = new Gson();
		String json = "";
		users = usuarioService.consultaUsuarioPorRol("Repartidor/Recolector");
        if (users != null) {
            users.forEach(user -> {
                user.setPassword(null);
            });
        }
		json = gson.toJson(users);
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@RequestMapping(value = "/courier", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> obtenerCourier() {
        List<UserDto> users;
		Gson gson = new Gson();
		String json = "";
		users = usuarioService.consultaUsuarioPorRol("Courier");
        if (users != null) {
            users.forEach(user -> {
                user.setPassword(null);
            });
        }
		json = gson.toJson(users);
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@RequestMapping(value = "/vendedor", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> obtenerVendedor() {
        List<UserDto> users;
		Gson gson = new Gson();
		String json = "";
		users = usuarioService.consultaUsuarioPorRol("Vendedor");
        if (users != null) {
            users.forEach(user -> {
                user.setPassword(null);
            });
        }
		json = gson.toJson(users);
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@RequestMapping(value = "/registro/list", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> consultaRegistro(
			@RequestParam(name = "from", required = false) Date fDate,
			@RequestParam(name = "to", required = false) Date tDate,
			@RequestParam(name = "estatus", required = true) Integer idEstatus,
			@RequestParam(name = "courier", required = false) String courier,
			@RequestParam(name = "customer", required = false) List<String> customer) {

		if (fDate == null || tDate == null) {
			return obtenerRegistros(idEstatus, customer, courier);
		}
		List<RegistroServiceOutDto> registros = registroService.consultaRegistro(fDate, tDate, customer, idEstatus,
				courier);

		return new ResponseEntity<>((new Gson()).toJson(registros), HttpStatus.OK);
	}

	@RequestMapping(value = "/registro/agenda/solicitar", method = RequestMethod.PUT, produces = {
			"application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> solicitarAgenda(@RequestBody List<ScheduleServiceInDto> scheduleList) {
		List<RegistryDto> registros = new ArrayList<>();
		List<DBResponse> respuesta = new ArrayList<>();

		for (ScheduleServiceInDto schedule : scheduleList) {
			RegistryDto registro = registroService.buscarPorOrderKey(new ObjectId(schedule.getOrderkey()));
            if (registro == null) {
                log.warn("No se encontró registro para la orden {} en solicitarAgenda", schedule.getOrderkey());
                respuesta.add(new DBResponse(false, "No se encontró registro para la orden " + schedule.getOrderkey()));
                continue;
            }
            if (registro.getOrder() == null) {
                log.warn("OrderDto es nulo para el registro {} en solicitarAgenda", registro.getId());
                respuesta.add(new DBResponse(false, "Datos de la orden incompletos para el registro " + registro.getId()));
                continue;
            }
			EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());
            if (actual == null) {
                log.warn("No se encontró estatus actual para el registro {} con idEstatus {}", registro.getId(), registro.getIdEstatus());
                respuesta.add(new DBResponse(false, "No se encontró estatus actual para el registro " + registro.getId()));
                continue;
            }
			EstatusDto siguiente = estatusService.obtenerEstatusSiguiente(actual);
            if (siguiente == null) {
                log.warn("No se encontró estatus siguiente para el estatus actual {}", actual.getId());
                respuesta.add(new DBResponse(false, "No se pudo determinar el siguiente estatus para el registro " + registro.getId()));
                continue;
            }

			// Integer idEstatusOriginal = actual.getId(); // Ya no es necesario aquí
			Integer idEstatusNuevo = siguiente.getId();

			EstatusLogDto estatusLog = new EstatusLogDto();
			estatusLog.setEstatusAnterior(actual.getId()); // Usar actual.getId() directamente
			estatusLog.setEstatusActual(idEstatusNuevo);
			estatusLog.setFechaActualizacion(new Date());
			estatusLog.setUsuario(schedule.getUser());
			List<EstatusLogDto> estatusLogList = registro.getEstatusLog();
			if (estatusLogList == null) {
				estatusLogList = new ArrayList<>();
            }
			estatusLogList.add(estatusLog);
			registro.setEstatusLog(estatusLogList);

			ScheduledDto scheduled = registro.getScheduled(); // Intentar obtener el existente
			if (scheduled == null) { // Si no existe, crear uno nuevo
				scheduled = new ScheduledDto();
				registro.setScheduled(scheduled);
			}
            // Actualizar campos del schedule según la información recibida
            if (schedule.getScheduledDate() != null) {
                scheduled.setScheduledDate(schedule.getScheduledDate());
            }
            if (schedule.getVendor() != null) {
                scheduled.setIdVendor(schedule.getVendor());
            }
            if (schedule.getComment() != null && !schedule.getComment().isEmpty()) {
                scheduled.setComment(schedule.getComment());
            }
            // scheduled.setAccepted(false); // Ajustar si es necesario para este flujo

			registro.setIdEstatus(idEstatusNuevo);

			registros.add(registro);
		} // Fin del bucle for

		// Guardar todos los registros modificados y construir la respuesta
		for (RegistryDto registroGuardado : registros) {
			RegistryDto registroResponse = registroService.guardar(registroGuardado);
            DBResponse responseItem = new DBResponse(); // Renombrado para evitar confusión con la lista 'respuesta'
			if (registroResponse != null) {
				responseItem.setResponse(true);
				responseItem.setResponseMessage("El registro [" + (registroGuardado.getOrder() != null ? registroGuardado.getOrder().getName() : registroGuardado.getId()) + "] se actualizó éxitosamente");
			} else {
				responseItem.setResponse(false);
				responseItem.setResponseMessage("El registro [" + (registroGuardado.getOrder() != null ? registroGuardado.getOrder().getName() : registroGuardado.getId()) + "] no se pudo actualizar");
			}
            respuesta.add(responseItem);
		}
		return new ResponseEntity<>((new Gson()).toJson(respuesta), HttpStatus.OK);
	}

	@Transactional
	@RequestMapping(value = "/registro/agenda/aceptar", method = RequestMethod.PUT, produces = {
			"application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> aceptarAgenda(@RequestBody List<ScheduleServiceInDto> scheduleList) {
		List<RegistryDto> registros = new ArrayList<>();
		for (ScheduleServiceInDto schedule : scheduleList) {
			RegistryDto registro = registroService.buscarPorOrderKey(new ObjectId(schedule.getOrderkey()));
            if (registro == null) {
                log.warn("No se encontró registro para la orden {} en aceptarAgenda", schedule.getOrderkey());
                continue;
            }
             if (registro.getOrder() == null) {
                log.warn("OrderDto es nulo para el registro {} en aceptarAgenda", registro.getId());
                continue;
            }
			EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());
            if (actual == null) {
                log.warn("No se encontró estatus actual para el registro {} con idEstatus {}", registro.getId(), registro.getIdEstatus());
                continue;
            }
            EstatusDto siguiente = estatusService.obtenerEstatusSiguiente(actual);
            if (siguiente == null) {
                log.warn("No se encontró estatus siguiente para el estatus actual {}", actual.getId());
                continue;
            }
			EstatusLogDto estatusLog = new EstatusLogDto();
			estatusLog.setEstatusAnterior(actual.getId());
			estatusLog.setEstatusActual(siguiente.getId());
			estatusLog.setFechaActualizacion(new Date());
			estatusLog.setUsuario(schedule.getUser());
			List<EstatusLogDto> estatusLogList = registro.getEstatusLog();
			if (estatusLogList == null) {
				estatusLogList = new ArrayList<>();
            }
			estatusLogList.add(estatusLog);
			registro.setEstatusLog(estatusLogList);
			ScheduledDto scheduled = registro.getScheduled();
            if (scheduled == null) {
                scheduled = new ScheduledDto();
                 registro.setScheduled(scheduled);
            }
                        scheduled.setAccepted(true);
                        scheduled.setIdCourier(schedule.getCourier());
                        if (schedule.getComment() != null && !schedule.getComment().isEmpty()) {
                                scheduled.setComment(schedule.getComment());
                        }
                        registro.setIdEstatus(siguiente.getId());

                        if (registro.getOrder() != null && registro.getOrder().isShopifyOrder()) {
                            VendorDto vendor = getVendor(registro);
                            if (vendor != null) {
                                try {
                                    ShopifyFulfillmentData data = shopifySyncService.fetchFulfillmentData(vendor, registro.getOrder().getId(), registro.getOrder());
                                    if (data != null) {
                                        // Los GIDs ya se guardan dentro de fetchFulfillmentDataGraphQL
                                        // Estos setters aseguran compatibilidad con la implementación REST y la lógica subsecuente
                                        registro.getOrder().setFulfillmentOrderId(data.getFulfillmentOrderId());
                                        registro.getOrder().setFulfillmentLineItemId(data.getLineItemId());
                                        registro.getOrder().setFulfillmentLineItemQty(data.getQuantity());
                                    } else {
                                        // Esto puede ocurrir si la implementación REST devuelve null pero no lanza excepción
                                        log.warn("La obtención de datos de Shopify para la orden {} devolvió null.", registro.getOrder().getId());
                                    }
                                } catch (ShopifyApiException e) {
                                    log.error("Error de API de Shopify al obtener datos para la orden {}: {}", registro.getOrder().getId(), e.getMessage());
                                    registro.getOrder().setShopifyApiError(e.getMessage());
                                }
                            }
                        }

                        registros.add(registro);
                }
		List<DBResponse> respuesta = new ArrayList<>();
		for (RegistryDto registroGuardado : registros) {
			DBResponse response = new DBResponse();
			try {
				RegistryDto registroResponse = registroService.guardar(registroGuardado);
				if (registroResponse != null) {
					response.setResponse(true);
					response.setResponseMessage("El registro [" + (registroGuardado.getOrder() != null ? registroGuardado.getOrder().getName() : registroGuardado.getId()) + "] se actualizó éxitosamente");
				} else {
					response.setResponse(false);
					response.setResponseMessage("El registro [" + (registroGuardado.getOrder() != null ? registroGuardado.getOrder().getName() : registroGuardado.getId()) + "] no se pudo actualizar");
				}
			} catch (Exception e) {
				log.error("Error al guardar el registro para la orden {}: {}", registroGuardado.getOrder() != null ? registroGuardado.getOrder().getId() : "N/A", e.getMessage());
				response.setResponse(false);
				response.setResponseMessage("Falla crítica al guardar el registro [" + (registroGuardado.getOrder() != null ? registroGuardado.getOrder().getName() : registroGuardado.getId()) + "].");
			}

			// Adjuntar errores de Shopify si existen
			if (registroGuardado.getOrder() != null && registroGuardado.getOrder().getShopifyApiError() != null) {
				String currentMessage = response.getResponseMessage();
				response.setResponseMessage(currentMessage + " | ADVERTENCIA SHOPIFY: " + registroGuardado.getOrder().getShopifyApiError());
				// Limpiar el error para no guardarlo en la BD
                registroGuardado.getOrder().setShopifyApiError(null);
			}

			respuesta.add(response);
		}
		return new ResponseEntity<>((new Gson()).toJson(respuesta), HttpStatus.OK);
	}

	@Transactional
	@RequestMapping(value = "/registro/agenda/rechazar", method = RequestMethod.PUT, produces = {
			"application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> rechazarAgenda(@RequestBody List<ScheduleServiceInDto> scheduleList) {
		List<RegistryDto> registros = new ArrayList<>();
		for (ScheduleServiceInDto scheduleServiceInDto : scheduleList) {
			RegistryDto registro = registroService.buscarPorOrderKey(new ObjectId(scheduleServiceInDto.getOrderkey()));
            if (registro == null) {
                log.warn("No se encontró registro para la orden {} en rechazarAgenda", scheduleServiceInDto.getOrderkey());
                continue;
            }
            if (registro.getOrder() == null) {
                log.warn("OrderDto es nulo para el registro {} en rechazarAgenda", registro.getId());
                continue;
            }
			EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());
            if (actual == null) {
                log.warn("No se encontró estatus actual para el registro {} con idEstatus {}", registro.getId(), registro.getIdEstatus());
                continue;
            }
			EstatusDto siguiente = estatusService.obtenerEstatusSiguienteExcepcion(actual);
            if (siguiente == null) {
                log.warn("No se encontró estatus siguiente (excepción) para el estatus actual {}", actual.getId());
                continue;
            }
			EstatusLogDto estatusLog = new EstatusLogDto();
			estatusLog.setEstatusAnterior(actual.getId());
			estatusLog.setEstatusActual(siguiente.getId());
			estatusLog.setFechaActualizacion(new Date());
			estatusLog.setUsuario(scheduleServiceInDto.getUser());
			List<EstatusLogDto> estatusLogList = registro.getEstatusLog();
			if (estatusLogList == null) {
				estatusLogList = new ArrayList<>();
            }
			estatusLogList.add(estatusLog);
			registro.setEstatusLog(estatusLogList);
			ScheduledDto scheduled = registro.getScheduled();
            if (scheduled == null) {
                scheduled = new ScheduledDto();
                registro.setScheduled(scheduled);
            }
			scheduled.setAccepted(false);
			scheduled.setComment(scheduleServiceInDto.getComment());
			scheduled.setScheduledDate(null);
			registro.setIdEstatus(siguiente.getId());
			registros.add(registro);
		}
		List<DBResponse> respuesta = new ArrayList<>();
		for (RegistryDto registroGuardado : registros) {
			RegistryDto registroResponse = registroService.guardar(registroGuardado);
			DBResponse response = new DBResponse();
			if (registroResponse != null) {
				response.setResponse(true);
				response.setResponseMessage("El registro [" + (registroGuardado.getOrder() != null ? registroGuardado.getOrder().getName() : registroGuardado.getId()) + "] se actualizó éxitosamente");
			} else {
				response.setResponse(false);
				response.setResponseMessage("El registro [" + (registroGuardado.getOrder() != null ? registroGuardado.getOrder().getName() : registroGuardado.getId()) + "] no se pudo actualizar");
			}
            respuesta.add(response);
		}
		return new ResponseEntity<>((new Gson()).toJson(respuesta), HttpStatus.OK);
	}

	@RequestMapping(value = "/registro/{hexString}", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<RegistryDto> consultaRegistro(@PathVariable String hexString) {
		ObjectId orderKey = new ObjectId(hexString);
		RegistryDto registro = registroService.buscarPorOrderKey(orderKey);
		return new ResponseEntity<>(registro, HttpStatus.OK);
	}

	@RequestMapping(value = "/guest/registro/{hexString}/estatus-log", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> consultaEstatusLogRegistro(@PathVariable String hexString) {
		ObjectId orderKey = null;
		Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("hh:mm:ss a")
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		try {
			orderKey = new ObjectId(hexString);
		} catch (Exception e) {
			DBResponse response = new DBResponse(false, "Formato inválido de clave de rastreo");
			return new ResponseEntity<>(gson.toJson(response), HttpStatus.BAD_REQUEST);
		}
		RegistryDto registro = registroService.buscarPorOrderKey(orderKey);
		if (registro == null) {
			DBResponse response = new DBResponse(false, "No se encontro la clave de rastreo");
			return new ResponseEntity<>(gson.toJson(response), HttpStatus.BAD_REQUEST);
		}
		List<EstatusLogDto> estatusLogs = registro.getEstatusLog();
		List<EstatusLogDataDto> logs = new ArrayList<>();
        if (estatusLogs != null) {
		    estatusLogs.forEach(estatusLog -> {
			    if (estatusLog.getFechaActualizacion() != null) {
				    EstatusLogDataDto estatusLogServiceDto;
				    EstatusDto estatus = estatusService.obtenerEstatusPorId(estatusLog.getEstatusActual());
				    estatusLogServiceDto = EstatusMapper.estatusLogDtoToEstatusLogServiceDto(estatusLog, estatus);
				    logs.add(estatusLogServiceDto);
			    }
		    });
        }
		Collections.sort(logs);
		Map<LocalDate, List<EstatusLogDataDto>> result = logs.stream()
            .filter(logEntry -> logEntry.getFecha() != null)
            .collect(Collectors.groupingBy(item -> convertToLocalDateViaInstant(item.getFecha())
						.with(TemporalAdjusters.ofDateAdjuster(d -> d))));
		List<HistoricoDto> historico = new ArrayList<>();
		result.keySet().forEach(key -> {
			List<EstatusLogDataDto> list = result.get(key);
			Collections.sort(list, Collections.reverseOrder());
			HistoricoDto h = new HistoricoDto(key, list);
			historico.add(h);
		});
		Collections.sort(historico, Collections.reverseOrder());
		EstatusLogServiceDto responseFramework = new EstatusLogServiceDto();
		EstatusDto estatusActual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());
		responseFramework.setEstatusActual(estatusActual);
		responseFramework.setHistorico(historico);
		responseFramework.setDestino(registro.getShipping_address());
		responseFramework.setOrigen(registro.getBilling_address());
		return new ResponseEntity<>(gson.toJson(responseFramework), HttpStatus.OK);
	}

	public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        if (dateToConvert == null) {
            return null;
        }
		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	@RequestMapping(value = "/comuna/list", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> consultaComunas() {
		List<String> comunas = new ArrayList<>();
		comunas = registroService.consultaComunas();
		Collections.sort(comunas);
		return new ResponseEntity<>((new Gson()).toJson(comunas), HttpStatus.OK);
	}
}
