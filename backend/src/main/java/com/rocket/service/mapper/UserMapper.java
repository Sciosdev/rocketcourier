package com.rocket.service.mapper;

import java.util.Base64;

import com.rocket.service.entity.UserDto;
import com.rocket.service.entity.VendorDto;
import com.rocket.service.model.UserServiceDto;
import com.rocket.service.model.UserTableServiceOutDto;

import org.bson.types.Binary;

public class UserMapper {

	public static UserTableServiceOutDto usuarioInDtoVendorDtoToUserServiceOutDto(UserDto usuarioInDto,
			VendorDto vendorDto) {

		UserTableServiceOutDto userServiceOutDto = new UserTableServiceOutDto();

		userServiceOutDto.setId(usuarioInDto.getId());
		userServiceOutDto.setNombre(usuarioInDto.getName());
		userServiceOutDto.setCorreo(usuarioInDto.getEmail());
		userServiceOutDto.setTelefono(usuarioInDto.getPhoneNumber());
		userServiceOutDto.setUsername(usuarioInDto.getUser());
		userServiceOutDto.setRol(usuarioInDto.getRol());
		userServiceOutDto.setTienda(vendorDto.getNombreTienda());

		return userServiceOutDto;
	}

	public static UserDto mapUserServiceDtoToUserDto(UserServiceDto userServiceDto) {
		UserDto userDto = new UserDto();

		userDto.setId(userServiceDto.getId());
		userDto.setUser(userServiceDto.getUser());
		userDto.setName(userServiceDto.getName());
		userDto.setRol(userServiceDto.getRol());
		userDto.setPassword(userServiceDto.getPassword());
		userDto.setTienda(userServiceDto.getTienda());
		userDto.setFirstName(userServiceDto.getFirstName());
		userDto.setLastName(userServiceDto.getLastName());
		userDto.setSecondLastName(userServiceDto.getSecondLastName());
		userDto.setDocumentType(userServiceDto.getDocumentType());
		userDto.setDocumentCountry(userServiceDto.getDocumentCountry());
		userDto.setDocumentNumber(userServiceDto.getDocumentNumber());
		userDto.setDv(userServiceDto.getDv());
		userDto.setBirthday(userServiceDto.getBirthday());
		userDto.setCommune(userServiceDto.getCommune());
		userDto.setPatent(userServiceDto.getPatent());
		userDto.setPhoneNumber(userServiceDto.getPhoneNumber());
		userDto.setVehicleData(userServiceDto.getVehicleData());
		userDto.setEmail(userServiceDto.getEmail());
		userDto.setActivo(userServiceDto.isActivo());
		userDto.setLastLogin(userServiceDto.getLastLogin());
		userDto.setFullAddress(userServiceDto.getFullAddress());
		if(userServiceDto.getFullAddress() != null)
		userDto.setAddress(userServiceDto.getFullAddress().toString());

		if (userServiceDto.getFoto() != null) {
			Binary binary = new Binary(Base64.getDecoder().decode(userServiceDto.getFoto()));
			userDto.setFoto(binary);
		}

		return userDto;
	}

	public static UserServiceDto mapUserDtoToUserServiceDto(UserDto userDto) {

		UserServiceDto userServiceDto = new UserServiceDto();

		userServiceDto.setId(userDto.getId());
		userServiceDto.setUser(userDto.getUser());
		userServiceDto.setName(userDto.getName());
		userServiceDto.setRol(userDto.getRol());
		userServiceDto.setPassword(userDto.getPassword());
		userServiceDto.setTienda(userDto.getTienda());
		userServiceDto.setFirstName(userDto.getFirstName());
		userServiceDto.setLastName(userDto.getLastName());
		userServiceDto.setSecondLastName(userDto.getSecondLastName());
		userServiceDto.setDocumentType(userDto.getDocumentType());
		userServiceDto.setDocumentCountry(userDto.getDocumentCountry());
		userServiceDto.setDocumentNumber(userDto.getDocumentNumber());
		userServiceDto.setDv(userDto.getDv());
		userServiceDto.setBirthday(userDto.getBirthday());
		userServiceDto.setAddress(userDto.getAddress());
		userServiceDto.setCommune(userDto.getCommune());
		userServiceDto.setPatent(userDto.getPatent());
		userServiceDto.setPhoneNumber(userDto.getPhoneNumber());
		userServiceDto.setVehicleData(userDto.getVehicleData());
		userServiceDto.setEmail(userDto.getEmail());
		userServiceDto.setActivo(userDto.isActivo());
		userServiceDto.setLastLogin(userDto.getLastLogin());
		userServiceDto.setFullAddress(userDto.getFullAddress());

		if (userDto.getFoto() != null)
			userServiceDto.setFoto(Base64.getEncoder().encodeToString(userDto.getFoto().getData()));

		return userServiceDto;
	}
}
