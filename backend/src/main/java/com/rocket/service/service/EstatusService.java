package com.rocket.service.service;

import java.util.ArrayList;
import java.util.List;

import com.rocket.service.entity.EstatusDto;
import com.rocket.service.repository.EstatusRepository;
import com.rocket.service.utils.TipoEstatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class EstatusService {

	@Autowired
	EstatusRepository repoEstatus;

	private MongoOperations mongoOperations;

	@Autowired
	public EstatusService(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	public List<EstatusDto> obtenerListaEstatus() {
		List<EstatusDto> estatus = new ArrayList<>();
		estatus = repoEstatus.findAll(Sort.by(Sort.Direction.ASC, "id"));
		return estatus;
	}

	public List<EstatusDto> obtenerEstatusPorTipo(TipoEstatus tipo) {
		List<EstatusDto> estatus = new ArrayList<>();
		estatus = repoEstatus.findByTipo(tipo.getDescripcion());
		return estatus;
	}

	public EstatusDto obtenerEstatusPorId(Integer id) {

		List<EstatusDto> estatus = mongoOperations.find(Query.query(Criteria.where("_id").is(id)), EstatusDto.class);

		if (estatus.isEmpty()) {
			EstatusDto st = new EstatusDto();
			st.setId(0);
			st.setDesc("Sin asignar");
			st.setTipo(TipoEstatus.EXCEPCION.getDescripcion());
			return st;
		} else
			return estatus.get(0);
	}

	public EstatusDto obtenerEstatusSiguiente(EstatusDto actual) {
		if (!actual.getTipo().equals(TipoEstatus.FINAL.getDescripcion())) {
			EstatusDto siguiente = new EstatusDto();
			siguiente = obtenerEstatusPorId(actual.getSiguiente());
			return siguiente;
		} else {
			return null;
		}
	}

	public EstatusDto obtenerEstatusSiguienteExcepcion(EstatusDto actual) {
		if (!actual.getTipo().equals(TipoEstatus.FINAL.getDescripcion())) {
			EstatusDto siguiente = new EstatusDto();
			siguiente = obtenerEstatusPorId(actual.getSiguienteExcepcion());
			if (siguiente.getId() == 0)
				return null;
			else
				return siguiente;
		} else {
			return null;
		}
	}

	public List<EstatusDto> obtenerEstatusSiguientes(EstatusDto actual) {
		List<EstatusDto> siguientes = new ArrayList<>();

		if (!actual.getTipo().equals(TipoEstatus.FINAL.getDescripcion())) {
			EstatusDto siguiente = new EstatusDto();
			siguiente = obtenerEstatusPorId(actual.getSiguiente());

			siguientes.add(siguiente);

			if (actual.getSiguienteExcepcion() != null) {
				EstatusDto siguienteException = new EstatusDto();
				siguienteException = obtenerEstatusPorId(actual.getSiguienteExcepcion());

				siguientes.add(siguienteException);
			}

		}

		return siguientes;
	}

	public EstatusDto obtenerEstatusInicial() {
		List<EstatusDto> estatus = obtenerEstatusPorTipo(TipoEstatus.INICIAL);

		if (estatus.isEmpty())
			return null;
		else
			return estatus.get(0);
	}
}
