package com.rocket.service.service;

import java.util.List;

import com.rocket.service.entity.LoadDto;
import com.rocket.service.repository.CargaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class CargaService {

	@Autowired
	CargaRepository repoCarga;


	private MongoOperations mongoOperations;

	@Autowired
	public CargaService(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}
	
	public LoadDto guardarCarga(LoadDto carga) {
		return repoCarga.save(carga);
	}

	public List<LoadDto> listarCargas() {
		return repoCarga.findAll();
	}

	public List<LoadDto> consultaCargaVendedor(String idVendor) {
		return repoCarga.findByIdVendor(idVendor);
	}

	public List<LoadDto> consultaCargaVendedor(List<String> customers) {
		List<LoadDto> cargas = mongoOperations.find(Query.query(Criteria.where("idVendor").in(customers)), LoadDto.class);

		return cargas;
	}
	
	public LoadDto obtenerCargaPorId(Long id) {
		List<LoadDto> cargas = mongoOperations.find(Query.query(Criteria.where("_id").is(id)), LoadDto.class);

		if (cargas.isEmpty()) {
			return new LoadDto();
		} else
			return cargas.get(0);
	}

}
