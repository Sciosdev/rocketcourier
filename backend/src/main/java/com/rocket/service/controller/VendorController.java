package com.rocket.service.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.gson.Gson;
import com.rocket.service.entity.UserDto;
import com.rocket.service.entity.VendorDto;
import com.rocket.service.mapper.VendorMapper;
import com.rocket.service.model.DBResponse;
import com.rocket.service.model.VendorCatalogServiceOutDto;
import com.rocket.service.model.VendorServiceDto;
import com.rocket.service.model.VendorCredentialsDto;
import com.rocket.service.service.SequenceGeneratorService;
import com.rocket.service.service.UsuarioService;
import com.rocket.service.service.VendorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VendorController {

	@Autowired
	VendorService service;

	@Autowired
	UsuarioService userService;

	@Autowired
	SequenceGeneratorService sequenceGeneratorService;

	@RequestMapping(value = "/vendor/{id}", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
	public String getPhoto(@PathVariable Integer id) {
		VendorDto photo = service.obtenerTiendaPorId(id);

		String image = Base64.getEncoder().encodeToString(photo.getLogo().getData());
		return image;
	}

	@RequestMapping(value = "/vendor", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
	public ResponseEntity<String> getTiendas() {
		List<VendorDto> vendorDtos = service.obtenerTiendas();
		List<VendorServiceDto> response = new ArrayList<>();

		vendorDtos.forEach(vendorDto -> {
			VendorServiceDto vendorServiceOutDto = VendorMapper.mapVendorDtoToVendorServiceDto(vendorDto);
			response.add(vendorServiceOutDto);
		});

		Gson gson = new Gson();
		String json = gson.toJson(response);
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@RequestMapping(value = "/vendor-catalog", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
        public ResponseEntity<String> getVendorCatalog() {
                List<VendorDto> vendorDtos = service.obtenerTiendas();
                List<VendorCatalogServiceOutDto> response = new ArrayList<>();

		vendorDtos.forEach(vendorDto -> {
			VendorCatalogServiceOutDto vendorCatalogServiceOutDto = VendorMapper.mapVendorDtoToVendorCatalogServiceOutDto(vendorDto);
			response.add(vendorCatalogServiceOutDto);
		});

		Gson gson = new Gson();
                String json = gson.toJson(response);
                return new ResponseEntity<>(json, HttpStatus.OK);
        }

        @RequestMapping(value = "/vendor/{id}/shopify", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
        public ResponseEntity<String> obtenerCredencialesShopify(@PathVariable Integer id) {
                VendorDto vendorDto = service.obtenerTiendaPorId(id);
                VendorCredentialsDto cred = new VendorCredentialsDto();
                cred.setShopifyApiKey(vendorDto.getShopifyApiKey());
                cred.setShopifyAccessToken(vendorDto.getShopifyAccessToken());

                Gson gson = new Gson();
                String json = gson.toJson(cred);
                return new ResponseEntity<>(json, HttpStatus.OK);
        }

        @RequestMapping(value = "/vendor/{id}/shopify", method = RequestMethod.PUT, produces = { "application/json;charset=UTF-8" })
        public ResponseEntity<String> actualizarCredencialesShopify(@PathVariable Integer id, @RequestBody VendorCredentialsDto cred) {
                VendorDto vendorDto = service.obtenerTiendaPorId(id);
                service.actualizarCredencialesShopify(vendorDto, cred.getShopifyApiKey(), cred.getShopifyAccessToken());

                Gson gson = new Gson();
                String json = gson.toJson(new DBResponse(true, "Credenciales actualizadas"));
                return new ResponseEntity<>(json, HttpStatus.OK);
        }

	@RequestMapping(value = "/vendor", method = RequestMethod.PUT, produces = { "application/json;charset=UTF-8" })
	public ResponseEntity<String> actualizarTienda(@RequestBody VendorServiceDto vendorServiceInDto) {
		VendorDto vendorDto = VendorMapper.mapVendorServiceDtoToVendorDto(vendorServiceInDto);

		VendorDto response = service.guardarTienda(vendorDto);

		Gson gson = new Gson();
		String json = gson.toJson(VendorMapper.mapVendorDtoToVendorServiceDto(response));
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@RequestMapping(value = "/vendor", method = RequestMethod.POST, produces = { "application/json;charset=UTF-8" })
	public ResponseEntity<String> altaTienda(@RequestBody VendorServiceDto vendorServiceInDto) {
		VendorDto vendorDto = VendorMapper.mapVendorServiceDtoToVendorDto(vendorServiceInDto);
		vendorDto.setId(sequenceGeneratorService.generateSequence(VendorDto.SEQUENCE_NAME));
		vendorDto.setActivo(true);

		VendorDto response = service.guardarTienda(vendorDto);

		Gson gson = new Gson();
		String json = gson.toJson(VendorMapper.mapVendorDtoToVendorServiceDto(response));
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@RequestMapping(value = "/vendor/activo", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	public ResponseEntity<String> actualizarActivoTiendas() {
		List<VendorDto> vendorDtos = service.obtenerTiendasF();
		List<VendorDto> response = new ArrayList<>();

		vendorDtos.forEach(vendorDto -> {
			response.add(service.setActivo(vendorDto, true));
		});

		Gson gson = new Gson();
		String json = gson.toJson(response);
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@RequestMapping(value = "/vendor/{id}", method = RequestMethod.DELETE, produces = {
			"application/json;charset=UTF-8" })
	public ResponseEntity<String> eliminarTienda(@PathVariable Integer id) {
		VendorDto vendorDto = service.obtenerTiendaPorId(id);

		VendorDto response = service.setActivo(vendorDto, false);

		List<UserDto> usuarios = userService.consultaUsuarioPorTienda(id);

		usuarios.forEach(usuario -> {
			usuario.setTienda(0);
			userService.guardarUsuario(usuario);
		});

		Gson gson = new Gson();
		String json;
		if (response != null) {
			json = gson.toJson(new DBResponse(true, "Recurso eliminado"));
		} else {
			json = gson.toJson(new DBResponse(false, "No se pudo eliminar el recurso"));
		}

		return new ResponseEntity<>(json, HttpStatus.OK);

	}

}
