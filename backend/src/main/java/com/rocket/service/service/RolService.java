package com.rocket.service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rocket.service.entity.RolDto;
import com.rocket.service.repository.RolesRepository;

@Service
public class RolService {

	@Autowired
	RolesRepository repoRol;

	public List<RolDto> consultaRol(String rol) {
		return repoRol.findByRol(rol);
	}

	public RolDto consultaRolPorId(String id) {
		Optional<RolDto> optRol = repoRol.findById(id);

		if (optRol.isPresent())
			return optRol.get();
		else
			return null;
	}

	public List<RolDto> consultaRol() {
		return repoRol.findAll();
	}

}
