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
	SequenceGeneratorService sequenceGeneratorService;

	@RequestMapping(value = "/registro", method = RequestMethod.POST, produces = {
			"application/json;charset=UTF-8" }, consumes = { "application/json" })
	public @ResponseBody LoadDto guardarRegistros(@RequestBody RegistroServiceInDto regis) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Para loguear bonito
		log.info("Endpoint /registro POST recibido. Payload: {}", gson.toJson(regis)); // LOG IMPORTANTE

		log.info("Tipo de carga: " + regis.getTipoCarga());

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
								|| registroService.validacion(registro).equals(" ")) {

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
						} else {
							errores.add("Error en fila [" + registro.getRowNumber() + "] : "
									+ registroService.validacion(registro));
							fallido.incrementAndGet();
						}

					} catch (Exception e) {
						fallido.incrementAndGet();
					}
				} else {
					omitido.incrementAndGet();
				}

			});
		} else {
			regis.getRegistro().forEach(registro -> {
				RegistryDto insercion;

				try {
					registro.setIdCarga(idCarga);
					registro.setId(sequenceGeneratorService.generateSequence(RegistryDto.SEQUENCE_NAME));
					registro.setIdEstatus(estatusInicial.getId());
					registro.getOrder().setOrderKey(new ObjectId());

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

				} catch (Exception e) {
					fallido.incrementAndGet();
				}

			});
		}
		Date date = new Date();

		if (exitoso.get() > 0) {
			regisCarga.setIdCarga(idCarga);
			regisCarga.setUploadDate(date);
			regisCarga.setRegistrosExitosos(exitoso.get());
			regisCarga.setRegistrosFallidos(fallido.get());
			regisCarga.setRegistrosOmitidos(omitido.get());
			if (errores.size() == 0) {
				regisCarga.setRespuesta("Registro Finalizado");
			} else {
				regisCarga.setRespuesta(StringUtils.join(errores, '|'));
			}
			regisCarga.setIdVendor(regis.getIdVendor());
			return cargaService.guardarCarga(regisCarga);
		} else {
			regisCarga.setIdCarga(null);
			regisCarga.setUploadDate(date);
			regisCarga.setRegistrosExitosos(exitoso.get());
			regisCarga.setRegistrosFallidos(fallido.get());
			regisCarga.setRegistrosOmitidos(omitido.get());
			regisCarga.setRespuesta(StringUtils.join(errores, '|'));
			regisCarga.setIdVendor(regis.getIdVendor());
			return regisCarga;
		}

	}

	private @ResponseBody ResponseEntity<String> obtenerRegistros(Integer idEstatus, List<String> customer,
			String courier) {
		List<RegistroServiceOutDto> registros = new ArrayList<RegistroServiceOutDto>();
		Gson gson = new Gson();
		String json = "";
		List<LoadDto> cargas;

		if (customer != null && !customer.isEmpty()) {

			cargas = cargaService.consultaCargaVendedor(customer);

			cargas.forEach(carga -> {
				List<RegistryDto> regis;
				regis = registroService.consultaRegistroCargaEstatus(carga.getIdCarga(), idEstatus, courier);

				regis.forEach(registro -> {

					RegistroServiceOutDto r = RegistroMapper.mapRegistroCargaToRegistroOut(registro, carga,
							usuarioService, vendorService, estatusService);

					registros.add(r);
				});
			});
		} else {

			List<RegistryDto> temp = registroService.consultaRegistroPorCourierYEstatus(courier, idEstatus);
			temp.forEach(registro -> {
				LoadDto carga = cargaService.obtenerCargaPorId(registro.getIdCarga());
				registros.add(RegistroMapper.mapRegistroCargaToRegistroOut(registro, carga, usuarioService,
						vendorService, estatusService));
			});
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
		users.forEach(user -> {
			user.setPassword(null);
		});
		json = gson.toJson(users);
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@RequestMapping(value = "/courier", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> obtenerCourier() {
		List<UserDto> users;
		Gson gson = new Gson();
		String json = "";
		users = usuarioService.consultaUsuarioPorRol("Courier");
		users.forEach(user -> {
			user.setPassword(null);
		});
		json = gson.toJson(users);
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@RequestMapping(value = "/vendedor", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> obtenerVendedor() {
		List<UserDto> users;
		Gson gson = new Gson();
		String json = "";
		users = usuarioService.consultaUsuarioPorRol("Vendedor");
		users.forEach(user -> {
			user.setPassword(null);
		});
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
		for (ScheduleServiceInDto schedule : scheduleList) {
			RegistryDto registro = new RegistryDto();
			registro = registroService.buscarPorOrderKey(new ObjectId(schedule.getOrderkey()));

			EstatusDto siguiente = new EstatusDto();
			EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());

			siguiente = estatusService.obtenerEstatusSiguiente(actual);

			EstatusLogDto estatusLog = new EstatusLogDto();
			estatusLog.setEstatusAnterior(actual.getId());
			estatusLog.setEstatusActual(siguiente.getId());
			estatusLog.setFechaActualizacion(new Date());
			estatusLog.setUsuario(schedule.getUser());

			List<EstatusLogDto> estatusLogList = registro.getEstatusLog();

			if (estatusLogList == null)
				estatusLogList = new ArrayList<>();

			estatusLogList.add(estatusLog);

			registro.setEstatusLog(estatusLogList);

			ScheduledDto scheduled = new ScheduledDto();
			scheduled.setScheduledDate(schedule.getScheduledDate());
			scheduled.setIdVendor(schedule.getVendor());
			scheduled.setComment(schedule.getComment());
			scheduled.setAccepted(false); // Por defecto todas nacen como no aceptadas
			registro.setScheduled(scheduled);
			registro.setIdEstatus(siguiente.getId());
			registros.add(registro);
		}

		List<DBResponse> respuesta = new ArrayList<>();

		for (RegistryDto registro : registros) {
			RegistryDto registroResponse = registroService.guardar(registro);
			if (registroResponse != null) {
				DBResponse response = new DBResponse();

				response.setResponse(true);
				response.setResponseMessage("El registro [" + registro.getId() + "] se actualizó éxitosamente");
				respuesta.add(response);
			} else {
				DBResponse response = new DBResponse();

				response.setResponse(false);
				response.setResponseMessage("El registro [" + registro.getId() + "] no se pudo actualizar");
				respuesta.add(response);
			}
		}

		return new ResponseEntity<>((new Gson()).toJson(respuesta), HttpStatus.OK);

	}

	@Transactional
	@RequestMapping(value = "/registro/agenda/aceptar", method = RequestMethod.PUT, produces = {
			"application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> aceptarAgenda(@RequestBody List<ScheduleServiceInDto> scheduleList) {
		List<RegistryDto> registros = new ArrayList<>();
		for (ScheduleServiceInDto schedule : scheduleList) {
			RegistryDto registro = new RegistryDto();
			registro = registroService.buscarPorOrderKey(new ObjectId(schedule.getOrderkey()));

			EstatusDto siguiente = new EstatusDto();
			EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());

			siguiente = estatusService.obtenerEstatusSiguiente(actual);

			EstatusLogDto estatusLog = new EstatusLogDto();
			estatusLog.setEstatusAnterior(actual.getId());
			estatusLog.setEstatusActual(siguiente.getId());
			estatusLog.setFechaActualizacion(new Date());
			estatusLog.setUsuario(schedule.getUser());

			List<EstatusLogDto> estatusLogList = registro.getEstatusLog();

			if (estatusLogList == null)
				estatusLogList = new ArrayList<>();

			estatusLogList.add(estatusLog);

			registro.setEstatusLog(estatusLogList);

			ScheduledDto scheduled = registro.getScheduled();

			scheduled.setAccepted(true);
			scheduled.setIdCourier(schedule.getCourier());
			if (schedule.getComment() != null && !schedule.getComment().isEmpty()) {
				scheduled.setComment(schedule.getComment());
			}
			registro.setScheduled(scheduled);
			registro.setIdEstatus(siguiente.getId());
			registros.add(registro);
		}

		List<DBResponse> respuesta = new ArrayList<>();

		for (RegistryDto registro : registros) {
			RegistryDto registroResponse = registroService.guardar(registro);
			if (registroResponse != null) {
				DBResponse response = new DBResponse();

				response.setResponse(true);
				response.setResponseMessage("El registro [" + registro.getOrder().getOrderKey().toHexString()
						+ "] se actualizó éxitosamente");
				respuesta.add(response);
			} else {
				DBResponse response = new DBResponse();

				response.setResponse(false);
				response.setResponseMessage(
						"El registro [" + registro.getOrder().getOrderKey().toHexString() + "] no se pudo actualizar");
				respuesta.add(response);
			}
		}

		return new ResponseEntity<>((new Gson()).toJson(respuesta), HttpStatus.OK);
	}

	@Transactional
	@RequestMapping(value = "/registro/agenda/rechazar", method = RequestMethod.PUT, produces = {
			"application/json;charset=UTF-8" })
	public @ResponseBody ResponseEntity<String> rechazarAgenda(@RequestBody List<ScheduleServiceInDto> scheduleList) {

		List<RegistryDto> registros = new ArrayList<>();
		for (ScheduleServiceInDto scheduleServiceInDto : scheduleList) {
			RegistryDto registro = new RegistryDto();

			registro = registroService.buscarPorOrderKey(new ObjectId(scheduleServiceInDto.getOrderkey()));

			EstatusDto siguiente = new EstatusDto();
			EstatusDto actual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());

			siguiente = estatusService.obtenerEstatusSiguienteExcepcion(actual);
			EstatusLogDto estatusLog = new EstatusLogDto();
			estatusLog.setEstatusAnterior(actual.getId());
			estatusLog.setEstatusActual(siguiente.getId());
			estatusLog.setFechaActualizacion(new Date());
			estatusLog.setUsuario(scheduleServiceInDto.getUser());

			List<EstatusLogDto> estatusLogList = registro.getEstatusLog();

			if (estatusLogList == null)
				estatusLogList = new ArrayList<>();

			estatusLogList.add(estatusLog);

			registro.setEstatusLog(estatusLogList);

			ScheduledDto scheduled = registro.getScheduled();
			scheduled.setAccepted(false);
			scheduled.setComment(scheduleServiceInDto.getComment());
			scheduled.setScheduledDate(null);

			registro.setScheduled(scheduled);
			registro.setIdEstatus(siguiente.getId());

			registros.add(registro);
		}

		List<DBResponse> respuesta = new ArrayList<>();

		for (RegistryDto registro : registros) {
			RegistryDto registroResponse = registroService.guardar(registro);
			DBResponse response = new DBResponse();

			if (registroResponse != null) {

				response.setResponse(true);
				response.setResponseMessage("El registro [" + registro.getOrder().getOrderKey().toHexString()
						+ "] se actualizó éxitosamente");
				respuesta.add(response);
			} else {

				response.setResponse(false);
				response.setResponseMessage(
						"El registro [" + registro.getOrder().getOrderKey().toHexString() + "] no se pudo actualizar");
				respuesta.add(response);
			}
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

		estatusLogs.forEach(estatusLog -> {

			if (estatusLog.getFechaActualizacion() != null) {
				EstatusLogDataDto estatusLogServiceDto;
				EstatusDto estatus = estatusService.obtenerEstatusPorId(estatusLog.getEstatusActual());
				estatusLogServiceDto = EstatusMapper.estatusLogDtoToEstatusLogServiceDto(estatusLog, estatus);
				logs.add(estatusLogServiceDto);
			}
		});

		Collections.sort(logs);

		Map<LocalDate, List<EstatusLogDataDto>> result = logs.stream().collect(Collectors
				.groupingBy(item -> convertToLocalDateViaInstant(item.getFecha())
						.with(TemporalAdjusters.ofDateAdjuster(d -> d))));

		List<HistoricoDto> historico = new ArrayList<>();

		result.keySet().forEach(key -> {
			List<EstatusLogDataDto> list = result.get(key);
			Collections.sort(list, Collections.reverseOrder());
			HistoricoDto h = new HistoricoDto(key, list);
			historico.add(h);
		});

		Collections.sort(historico, Collections.reverseOrder());

		EstatusLogServiceDto response = new EstatusLogServiceDto();

		EstatusDto estatusActual = estatusService.obtenerEstatusPorId(registro.getIdEstatus());
		response.setEstatusActual(estatusActual);
		response.setHistorico(historico);
		response.setDestino(registro.getShipping_address());
		response.setOrigen(registro.getBilling_address());

		return new ResponseEntity<>(gson.toJson(response), HttpStatus.OK);
	}

	public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
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
