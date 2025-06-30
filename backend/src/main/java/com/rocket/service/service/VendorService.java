package com.rocket.service.service;

import java.io.IOException;
import java.util.List;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rocket.service.entity.VendorDto;
import com.rocket.service.repository.TiendaRepository;

@Service
public class VendorService {

	@Autowired
	private TiendaRepository vendorRepository;

	private MongoOperations mongoOperations;

	@Autowired
	public VendorService(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}
	
	public VendorDto guardarTienda(VendorDto tienda, MultipartFile file) throws IOException {
		tienda.setLogo(
          new Binary(BsonBinarySubType.BINARY, file.getBytes())); 	
		
		return vendorRepository.save(tienda);
	}

	public VendorDto guardarTienda(VendorDto tienda){	
		return vendorRepository.save(tienda);
	}
	
	public VendorDto obtenerTiendaPorId(Integer id) {

		List<VendorDto> tiendas = mongoOperations.find(Query.query(Criteria.where("_id").is(id)), VendorDto.class);

		if (tiendas.isEmpty()) {
			return new VendorDto();
		} else
			return tiendas.get(0);
	}

	public List<VendorDto> obtenerTiendas() {
		List<VendorDto> tiendas = mongoOperations.find(Query.query(Criteria.where("activo").is(true)), VendorDto.class);

		return tiendas;
	}

	public List<VendorDto> obtenerTiendasF() {

		List<VendorDto> tiendas = vendorRepository.findAll();
		return tiendas;
	}

	public VendorDto setActivo(VendorDto tienda, Boolean activo){		
		tienda.setActivo(activo);

		return vendorRepository.save(tienda);
	}

}