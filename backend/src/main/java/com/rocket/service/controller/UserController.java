package com.rocket.service.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.rocket.service.entity.UserDto;
import com.rocket.service.entity.VendorDto;
import com.rocket.service.exceptions.BadUserException;
import com.rocket.service.mapper.UserMapper;
import com.rocket.service.model.DBResponse;
import com.rocket.service.model.UserServiceDto;
import com.rocket.service.model.UserTableServiceOutDto;
import com.rocket.service.service.SequenceGeneratorService;
import com.rocket.service.service.UsuarioService;
import com.rocket.service.service.VendorService;
import com.rocket.service.utils.PasswordStorage;
import com.rocket.service.utils.PasswordStorage.CannotPerformOperationException;
import com.rocket.service.utils.RoleName;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController @Slf4j
public class UserController {

	@Autowired
	UsuarioService service;

	@Autowired
	VendorService vendorService;

	@Autowired
	SequenceGeneratorService sequenceGeneratorService;

	@RequestMapping(value = "/user/{username}", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	public ResponseEntity<String> getUser(@PathVariable String username) {

		UserDto usuario = service.obtenerUsuario(username);

		if (usuario == null || (usuario.getRol() != null && usuario.getRol().equals(RoleName.ROOT.getValue()))) {
			log.error("No se encontro el usuario: {}", username);
			return new ResponseEntity<>("No se encontro el usuario", HttpStatus.NO_CONTENT);
		}

		UserTableServiceOutDto userServiceOutDto = new UserTableServiceOutDto();
                VendorDto vendorDto = vendorService.obtenerTiendaPorId(usuario.getTienda() != null ? usuario.getTienda().longValue() : null);

		userServiceOutDto = UserMapper.usuarioInDtoVendorDtoToUserServiceOutDto(usuario, vendorDto);

		Gson gson = new Gson();
		return new ResponseEntity<>(gson.toJson(userServiceOutDto), HttpStatus.OK);
	}

	@RequestMapping(value = "/user-full/{username}", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	public ResponseEntity<String> getFullUser(@PathVariable String username) {

		UserDto userDto = service.obtenerUsuario(username);

		if (userDto == null || (userDto.getRol() != null && userDto.getRol().equals(RoleName.ROOT.getValue()))) {
			log.error("No se encontro el usuario: {}", username);
			return new ResponseEntity<>("No se encontro el usuario", HttpStatus.NO_CONTENT);
		}

		userDto.setPassword(null);
		userDto.setLastLogin(null);

		UserServiceDto userServiceDto = UserMapper.mapUserDtoToUserServiceDto(userDto);

		Gson gson = new Gson();
		return new ResponseEntity<>(gson.toJson(userServiceDto), HttpStatus.OK);
	}

	@RequestMapping(value = "/user", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
	public ResponseEntity<String> getUsers(@RequestParam(name = "rol", required = false) String rol,
			@RequestParam(name = "tienda", required = false) Integer tienda) {

		List<UserDto> users = new ArrayList<>();

		users = service.consultaUsuarios(rol, tienda);

		List<UserTableServiceOutDto> response = new ArrayList<>();

		for (UserDto user : users) {
			if (user.getRol() != null && (!user.getRol().equals(RoleName.ROOT.getValue()))) {
				UserTableServiceOutDto userServiceOutDto = new UserTableServiceOutDto();
                                VendorDto vendorDto = vendorService.obtenerTiendaPorId(user.getTienda() != null ? user.getTienda().longValue() : null);
				userServiceOutDto = UserMapper.usuarioInDtoVendorDtoToUserServiceOutDto(user, vendorDto);
				response.add(userServiceOutDto);
			}
		}

		log.debug("Obteniendo el listado de usuarios");
		Gson gson = new Gson();
		return new ResponseEntity<>(gson.toJson(response), HttpStatus.OK);
	}

	@RequestMapping(value = "/user-activo", method = RequestMethod.PUT, produces = { "application/json;charset=UTF-8" })
	public ResponseEntity<String> setUsersActivo() {

		List<UserDto> users = new ArrayList<>();

		users = service.obtenerUsuarios();

		for (UserDto user : users) {

			user.setActivo(true);
			service.guardarUsuario(user);
		}

		log.warn("Activando todos los usuarios");


		Gson gson = new Gson();
		return new ResponseEntity<>(gson.toJson("Usuarios Actualizados a true"), HttpStatus.OK);
	}

	@RequestMapping(value = "/user", method = RequestMethod.POST, produces = { "application/json;charset=UTF-8" })
	public ResponseEntity<String> altaUsuario(@RequestBody UserServiceDto userServiceDto) {
		UserDto userDto = UserMapper.mapUserServiceDtoToUserDto(userServiceDto);

		ObjectId id = new ObjectId();
		userDto.setId(id.toHexString());

		userDto.setActivo(true);

		try {
			String password = PasswordStorage.createHash(userDto.getPassword());
			userDto.setPassword(password);
		} catch (CannotPerformOperationException e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		UserDto response = new UserDto();

		try {
			response = service.guardarUsuario(userDto);
		} catch (DuplicateKeyException e) {
			log.error(e.getMessage());
			return new ResponseEntity<>("Nombre de usuario duplicado", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.setPassword(null);

		log.info("Usuario [{}] dado de alta con exito", response.getUser());
		Gson gson = new Gson();
		String json = gson.toJson(response);
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@RequestMapping(value = "/user", method = RequestMethod.PUT, produces = { "application/json;charset=UTF-8" })
	public ResponseEntity<String> updateUser(@RequestBody UserServiceDto userServiceDto) {

		UserDto userIn = UserMapper.mapUserServiceDtoToUserDto(userServiceDto);

		UserDto usuario = service.obtenerUsuario(userIn.getUser());

		if (userIn.getPassword() != null && !userIn.getPassword().trim().isEmpty()) {
			try {
				String password = PasswordStorage.createHash(userIn.getPassword());
				userIn.setPassword(password);
			} catch (CannotPerformOperationException e) {
				log.error(e.getMessage());
				return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			userIn.setPassword(usuario.getPassword());
		}

		userIn.setLastLogin(usuario.getLastLogin());

		UserDto response = service.guardarUsuario(userIn);

		response.setPassword(null);
		response.setLastLogin(null);

		log.info("Usuario [{}] actualizado con exito", response.getUser());
		Gson gson = new Gson();

		return new ResponseEntity<>(gson.toJson(response), HttpStatus.OK);
	}

	@RequestMapping(value = "/user/{username}", method = RequestMethod.DELETE, produces = {
			"application/json;charset=UTF-8" })
	public ResponseEntity<String> deleteUser(@PathVariable String username, Authentication authentication)
			throws IOException {

		Gson gson = new Gson();
		DBResponse response = new DBResponse();

		String loggedUser = authentication.getName();

		if (username.trim().equals(loggedUser.trim())) {
			response.setResponse(false);
			response.setResponseMessage("No se puede eliminar al usuario en sesión");
			log.error("No se puede eliminar al usuario en sesión");
			return new ResponseEntity<>(gson.toJson(response), HttpStatus.OK);
		}

		UserDto user = service.obtenerUsuario(username);

		if (user == null) {
			response.setResponse(false);
			response.setResponseMessage("Usuario no válido");
			log.error("Usuario no válido");
			return new ResponseEntity<>(gson.toJson(response), HttpStatus.OK);
		}

		if (user.getRol().equals(RoleName.ROOT.getValue())) {
			response.setResponse(false);
			response.setResponseMessage("No se tiene autorización para eliminar al usuario [" + RoleName.ROOT.getValue() + "]");
			log.error("No se tiene autorización para eliminar al usuario [" + RoleName.ROOT.getValue() + "]");
			return new ResponseEntity<>(gson.toJson(response), HttpStatus.BAD_REQUEST);
		}

		try {
			user.setActivo(false);
			service.guardarUsuario(user);

			response.setResponse(true);
			response.setResponseMessage("El usuario [" + username + "] fue borrado satisfactoriamente");
			log.info("El usuario [" + username + "] fue borrado satisfactoriamente");
		} catch (Exception e) {
			response.setResponse(false);
			if (BadUserException.class.isInstance(e)){
				response.setResponseMessage("Usuario no válido");
				log.info("Usuario no válido");
			}
			else{
				response.setResponseMessage(e.toString());
				log.info(e.toString());
			}
		}

		return new ResponseEntity<>(gson.toJson(response), HttpStatus.OK);
	}
}
