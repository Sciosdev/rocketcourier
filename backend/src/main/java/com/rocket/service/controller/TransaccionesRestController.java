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
import com.rocket.service.entity.EstatusDto;
import com.rocket.service.entity.EstatusLogDto;
import com.rocket.service.entity.LoadDto;
import com.rocket.service.entity.RegistryDto;
import com.rocket.service.entity.ScheduledDto;
import com.rocket.service.entity.UserDto;
import com.rocket.service.mapper.EstatusMapper;
import com.rocket.service.mapper.RegistroMapper;
import com.rocket.service.model.DBResponse;
import com.rocket.service.model.EstatusLogDataDto;
import com.rocket.service.model.EstatusLogServiceDto;
import com.rocket.service.model.HistoricoDto;
import com.rocket.service.model.RegistroServiceInDto;
import com.rocket.service.model.RegistroServiceOutDto;
import com.rocket.service.model.ScheduleServiceInDto;
import com.rocket.service.service.CargaService;
import com.rocket.service.service.EstatusService;
import com.rocket.service.service.RegistroService;
import com.rocket.service.service.RolService;
import com.rocket.service.service.SequenceGeneratorService;
import com.rocket.service.service.UsuarioService;
import com.rocket.service.service.VendorService;

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
// ... (otros imports necesarios como Slf4j, Autowired, RestController, RequestMapping, RequestBody, ResponseBody, ResponseEntity)
// ... (importar LoadDto, Date, etc.)
import com.google.gson.Gson; // Para el log del payload si es necesario
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.rocket.service.entity.EstatusDto;
import com.rocket.service.entity.LoadDto;
import com.rocket.service.entity.RegistryDto; // Asegúrate que esta y otras entidades estén importadas
import com.rocket.service.model.RegistroServiceInDto; // Aún lo necesitamos para el tipo de retorno de LoadDto
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;
import java.util.ArrayList; // Para errores en LoadDto
import java.util.List; // Para List
import java.util.concurrent.atomic.AtomicInteger; // Para contadores en LoadDto
import org.apache.commons.lang3.StringUtils; // Para StringUtils.join
import com.rocket.service.service.CargaService;
import com.rocket.service.service.EstatusService;
import com.rocket.service.service.RegistroService;
import com.rocket.service.service.SequenceGeneratorService;
import org.bson.types.ObjectId; // Si se usa directamente
import com.rocket.service.entity.EstatusLogDto;


@RestController
//@EnableTransactionManagement // Podemos comentarlo temporalmente si sospechamos de AOP
@Slf4j
public class TransaccionesRestController {

	@Autowired
	RegistroService registroService;

	@Autowired
	CargaService cargaService;

	@Autowired
	EstatusService estatusService;

	@Autowired
	SequenceGeneratorService sequenceGeneratorService;

	@RequestMapping(value = "/registro", method = RequestMethod.POST, produces = {
			"application/json;charset=UTF-8" }, consumes = { "application/json" })
	public @ResponseBody ResponseEntity<LoadDto> guardarRegistros(@RequestBody String requestBodyString) { // Cambiado a String
        log.info("====== INICIO Endpoint /registro POST ======");
        log.info("Raw Payload String: {}", requestBodyString);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") // Importante para leer el formato que ahora envía ShopifyController
                .create();

        RegistroServiceInDto regis;
        LoadDto responseDto = new LoadDto();
        responseDto.setUploadDate(new Date());
        responseDto.setRegistrosExitosos(0);
        responseDto.setRegistrosFallidos(0);
        responseDto.setRegistrosOmitidos(0);

        try {
            regis = gson.fromJson(requestBodyString, RegistroServiceInDto.class);
            if (regis == null) {
                log.error("Payload deserializado a null.");
                responseDto.setRespuesta("Error: El payload no pudo ser deserializado correctamente (resultado nulo).");
                responseDto.setRegistrosFallidos(1); // Indicar un error genérico de carga
                return ResponseEntity.badRequest().body(responseDto);
            }
            log.info("Payload deserializado con Gson. idVendor: {}, tipoCarga: {}, número de registros: {}",
                     regis.getIdVendor(), regis.getTipoCarga(), (regis.getRegistro() != null ? regis.getRegistro().size() : "null"));
            responseDto.setIdVendor(regis.getIdVendor());
            responseDto.setTipoCarga(regis.getTipoCarga());

        } catch (JsonSyntaxException e) {
            log.error("Error de sintaxis JSON deserializando el payload con Gson: ", e);
            responseDto.setRespuesta("Error de formato en JSON: " + e.getMessage());
            responseDto.setRegistrosFallidos(1); // Indicar un error genérico de carga
            return ResponseEntity.badRequest().body(responseDto);
        } catch (Exception e) {
            log.error("Error inesperado deserializando el payload con Gson: ", e);
            responseDto.setRespuesta("Error inesperado durante la deserialización: " + e.getMessage());
            responseDto.setRegistrosFallidos(1); // Indicar un error genérico de carga
            return ResponseEntity.badRequest().body(responseDto);
        }

        if (regis.getRegistro() == null || regis.getRegistro().isEmpty()) {
            log.info("La lista de registros está vacía o es nula. No hay nada que procesar.");
            responseDto.setRespuesta("No se enviaron registros para procesar o la lista de registros es nula.");
            // No es necesario guardar un LoadDto si no hay nada que cargar.
            return ResponseEntity.ok(responseDto);
        }

        // Si llegamos aquí, la deserialización fue exitosa y hay registros.
        // Por ahora, no los procesaremos, solo confirmaremos que llegaron.
        log.info("Deserialización exitosa. {} registros recibidos. Saltando lógica de guardado para depuración.", regis.getRegistro().size());
        responseDto.setRespuesta("Datos recibidos y deserializados correctamente. Número de registros: " + regis.getRegistro().size() + ". El guardado está desactivado para depuración.");
        responseDto.setRegistrosOmitidos(regis.getRegistro().size()); // Marcamos como omitidos para esta prueba

        // Simular una carga para obtener un ID, aunque no guardemos los registros individuales aún
        LoadDto tempCarga = new LoadDto();
        tempCarga.setIdCarga(sequenceGeneratorService.generateSequence(LoadDto.SEQUENCE_NAME));
        tempCarga.setTipoCarga(regis.getTipoCarga());
        tempCarga.setIdVendor(regis.getIdVendor());
        tempCarga.setUploadDate(new Date());
        tempCarga.setRegistrosExitosos(0);
        tempCarga.setRegistrosFallidos(0);
        tempCarga.setRegistrosOmitidos(regis.getRegistro().size());
        tempCarga.setRespuesta(responseDto.getRespuesta());
        LoadDto cargaGuardada = cargaService.guardarCarga(tempCarga);
        responseDto.setIdCarga(cargaGuardada.getIdCarga());

        log.info("====== FIN Endpoint /registro POST (Respuesta de depuración) ======");
        return ResponseEntity.ok(responseDto);

        // Aquí comenzaría la lógica original de procesamiento que está comentada por ahora
        /*
		LoadDto regisCarga = new LoadDto();
		List<String> errores = new ArrayList<>();

		EstatusDto estatusInicial = estatusService.obtenerEstatusInicial();

		Long idCarga;

		AtomicInteger exitoso = new AtomicInteger(0);
		AtomicInteger fallido = new AtomicInteger(0);
		AtomicInteger omitido = new AtomicInteger(0);

		regisCarga.setIdCarga(sequenceGeneratorService.generateSequence(LoadDto.SEQUENCE_NAME));
		regisCarga.setTipoCarga(regis.getTipoCarga());

		LoadDto inserted = cargaService.guardarCarga(regisCarga);

		idCarga = inserted.getIdCarga();

		if (regis.getTipoCarga() == 0) {

			regis.getRegistro().forEach(registro -> {
				RegistryDto insercion;
				if (registro.getOrder().getFinancial_status() != null
						&& registro.getOrder().getFinancial_status().equalsIgnoreCase("Paid")) {
					try {
						registro.setIdCarga(idCarga);
						registro.setId(sequenceGeneratorService.generateSequence(RegistryDto.SEQUENCE_NAME));
						registro.setIdEstatus(estatusInicial.getId());
						registro.getOrder().setOrderKey(new ObjectId());
						if (registroService.validacion(registro) == null
								|| registroService.validacion(registro).trim().isEmpty()) { // Ajustado para trim()

							EstatusLogDto estatusLog = new EstatusLogDto();
							estatusLog.setEstatusAnterior(null);
							estatusLog.setEstatusActual(registro.getIdEstatus());
							estatusLog.setFechaActualizacion(new Date());
							estatusLog.setUsuario(regis.getIdVendor());

							List<EstatusLogDto> estatusLogList = new ArrayList<>();

							estatusLogList.add(estatusLog);

							registro.setEstatusLog(estatusLogList);
							insercion = registroService.guardar(registro);
							if (insercion != null)
								exitoso.incrementAndGet();
                            else {
                                log.error("Error al guardar registro (insercion nula) para orden: {}", registro.getOrder() != null ? registro.getOrder().getName() : "ID DESCONOCIDO");
                                errores.add("Error en fila [" + (registro.getRowNumber() != null ? registro.getRowNumber() : "N/A") + "] : Falla desconocida al guardar.");
                                fallido.incrementAndGet();
                            }
						} else {
                            log.warn("Validación fallida para el pedido: {}. Errores: {}", (registro.getOrder() != null ? registro.getOrder().getName() : "ID DESCONOCIDO"), registroService.validacion(registro).trim());
							errores.add("Error en fila [" + (registro.getRowNumber() != null ? registro.getRowNumber() : "N/A") + "] : "
									+ registroService.validacion(registro).trim());
							fallido.incrementAndGet();
						}

					} catch (Exception e) {
                        log.error("Excepción procesando registro para orden: {}", (registro.getOrder() != null ? registro.getOrder().getName() : "ID DESCONOCIDO"), e);
                        errores.add("Error en fila [" + (registro.getRowNumber() != null ? registro.getRowNumber() : "N/A") + "] : " + e.getMessage());
						fallido.incrementAndGet();
					}
				} else {
                    log.info("Pedido omitido (financial_status no es 'Paid' o es nulo): {}", (registro.getOrder() != null ? registro.getOrder().getName() : "ID DESCONOCIDO"));
					omitido.incrementAndGet();
				}

			});
		} else { // Lógica para tipoCarga != 0 (CSV)
			regis.getRegistro().forEach(registro -> {
				RegistryDto insercion;

				try {
					registro.setIdCarga(idCarga);
					registro.setId(sequenceGeneratorService.generateSequence(RegistryDto.SEQUENCE_NAME));
					registro.setIdEstatus(estatusInicial.getId());
                    if (registro.getOrder().getOrderKey() == null) {
                       registro.getOrder().setOrderKey(new ObjectId());
                    }
                    // Aquí podrías añadir la validación de registroService.validacion(registro) si también aplica para CSVs
                    // o mantener la lógica original si la validación de CSV es diferente.
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
                         errores.add("Error en fila [" + (registro.getRowNumber() != null ? registro.getRowNumber() : "N/A") + "] : Falla desconocida al guardar (CSV).");
                    }
				} catch (Exception e) {
                    log.error("Excepción procesando registro CSV: ", e);
                    errores.add("Error en fila [" + (registro.getRowNumber() != null ? registro.getRowNumber() : "N/A") + "] : " + e.getMessage());
					fallido.incrementAndGet();
				}

			});
		}
		Date date = new Date();

        regisCarga.setUploadDate(date);
        regisCarga.setRegistrosExitosos(exitoso.get());
        regisCarga.setRegistrosFallidos(fallido.get());
        regisCarga.setRegistrosOmitidos(omitido.get());
        if (errores.isEmpty()) {
            if (exitoso.get() > 0 || omitido.get() > 0) { // Si hubo algo que procesar
                regisCarga.setRespuesta("Registro Finalizado");
            } else if (fallido.get() == 0 && omitido.get() == 0 && exitoso.get() == 0 && regis.getRegistro().isEmpty()){
                 regisCarga.setRespuesta("No se enviaron registros para procesar.");
            } else {
                 regisCarga.setRespuesta("No se procesaron registros."); // Caso genérico si todo es 0 pero la lista no estaba vacía inicialmente
            }
        } else {
            regisCarga.setRespuesta(StringUtils.join(errores, " | "));
        }
        regisCarga.setIdVendor(regis.getIdVendor());

        if (exitoso.get() > 0 || fallido.get() > 0 || omitido.get() > 0) {
            return cargaService.guardarCarga(regisCarga); // Actualizar o guardar el LoadDto final
        } else {
            // Si no se procesó nada y la lista original estaba vacía, no guardamos la carga.
            // Devolvemos el regisCarga sin ID persistido.
            regisCarga.setIdCarga(null); // Aseguramos que no tenga ID de carga si no se procesó nada.
            return regisCarga;
        }
        */
	}

	private @ResponseBody ResponseEntity<String> obtenerRegistros(Integer idEstatus, List<String> customer,
			String courier) {
		List<RegistroServiceOutDto> registros = new ArrayList<RegistroServiceOutDto>();
		Gson gson = new Gson(); // Cambiado de GsonBuilder a Gson simple
		String json = "";
		List<LoadDto> cargas;
