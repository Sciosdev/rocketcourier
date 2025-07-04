package com.rocket.service.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rocket.service.entity.UserDto;
import com.rocket.service.exceptions.BadUserException;
import com.rocket.service.repository.UserRepository;

@Service
public class UsuarioService {

	@Autowired
	UserRepository repoUser;

	private MongoOperations mongoOperations;

	public UsuarioService(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	public List<UserDto> consulta(String username) {
		return repoUser.findByUser(username);
	}

	public List<UserDto> obtenerUsuarios() {
		
		List<UserDto> result = repoUser.findAll();
		List<UserDto> resultActivos = new ArrayList<>();

		result.forEach(user -> { 
			if(user.isActivo())
			resultActivos.add(user);
		});

		return resultActivos;
	}

	public UserDto obtenerUsuario(String username) {

		UserDto user = new UserDto();

		List<UserDto> result = consulta(username);

		if (result.isEmpty())
			return null;

		user = result.get(0);

		if (user.isActivo())
			return user;
		else
			return null;
	}

	public UserDto consultaPorId(String id) {
		return repoUser.findById(id).get();
	}

	public List<UserDto> consultaUsuarioPorRol(String rol) {
		
		List<UserDto> result = repoUser.findByUserForRol(rol);
		List<UserDto> resultActivos = new ArrayList<>();

		result.forEach(user -> { 
			if(user.isActivo())
			resultActivos.add(user);
		});

		return resultActivos;
	}

	public List<UserDto> consultaUsuarios(String rol, Integer tienda) {

		List<UserDto> usuarios = new ArrayList<>();

		if (rol != null && !rol.trim().isEmpty() && tienda != null)
			usuarios = mongoOperations.find(
					Query.query(Criteria.where("activo").is(true).and("rol").is(rol).and("tienda").is(tienda)),
					UserDto.class);
		else if (rol != null && !rol.trim().isEmpty())
			usuarios = mongoOperations.find(Query.query(Criteria.where("activo").is(true).and("rol").is(rol)),
					UserDto.class);
		else if (tienda != null)
			usuarios = mongoOperations.find(Query.query(Criteria.where("activo").is(true).and("tienda").is(tienda)),
					UserDto.class);
		else
			usuarios = mongoOperations.find(Query.query(Criteria.where("activo").is(true)), UserDto.class);

		return usuarios;
	}

	public UserDto guardarUsuario(String username, MultipartFile file) throws IOException {

		UserDto user = new UserDto();

		List<UserDto> result = consulta(username);

		if (result.isEmpty())
			return null;

		user = result.get(0);

		user.setFoto(new Binary(BsonBinarySubType.BINARY, file.getBytes()));

		return repoUser.save(user);
	}

	public UserDto guardarUsuario(UserDto user) {
		return repoUser.save(user);
	}

	public void eliminarUsuario(String username) {
		UserDto user = new UserDto();

		List<UserDto> result = consulta(username);

		if (result.isEmpty())
			throw new BadUserException();

		user = result.get(0);

		repoUser.deleteById(user.getId());
	}

        public List<UserDto> consultaUsuarioPorTienda(Long idTienda) {
                List<UserDto> usuarios = mongoOperations
                                .find(Query.query(Criteria.where("tienda").is(idTienda).and("activo").is(true)), UserDto.class);
                return usuarios;
        }
}
