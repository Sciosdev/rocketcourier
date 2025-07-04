package com.rocket.service.mapper;

import java.util.List;

import com.rocket.service.entity.LoadDto;
import com.rocket.service.entity.EstatusDto;
import com.rocket.service.entity.RegistryDto;
import com.rocket.service.entity.VendorDto;
import com.rocket.service.model.RegistroServiceOutDto;
import com.rocket.service.entity.UserDto;
import com.rocket.service.service.EstatusService;
import com.rocket.service.service.UsuarioService;
import com.rocket.service.service.VendorService;

public class RegistroMapper {

	public static RegistroServiceOutDto mapRegistroCargaToRegistroOut(RegistryDto registro, LoadDto carga, UsuarioService usuarioService, VendorService vendorService, EstatusService estatusService) {

		List<UserDto> result = usuarioService.consulta(carga.getIdVendor());
		UserDto vendedor;
		String nombreTienda = "Sin información de vendedor";

		if (!result.isEmpty()) {
			vendedor = result.get(0);
                        VendorDto tienda = vendorService.obtenerTiendaPorId(vendedor.getTienda());
			nombreTienda = tienda.getNombreTienda();
		}

		RegistroServiceOutDto registroOut = new RegistroServiceOutDto();

		registroOut.setVendedor(nombreTienda);
		registroOut.setOrderkey(registro.getOrder().getOrderKey().toHexString());
		registroOut.setName(registro.getOrder().getName());
		registroOut.setEmail(registro.getOrder().getEmail());
		registroOut.setShippingCity(registro.getShipping_address().getCity());
		registroOut.setShippingAdress1(registro.getShipping_address().getAddress1());
		registroOut.setShippingAdress2(registro.getShipping_address().getAddress2());
		EstatusDto estatus = estatusService.obtenerEstatusPorId(registro.getIdEstatus());
		registroOut.setDescStatus(estatus.getDesc());
		registroOut.setCargaDt(carga.getUploadDate());

		if(registro.getDeliveryComment() != null) {
			registroOut.setDeliveryComment(registro.getDeliveryComment());
		}
		
		if (registro.getScheduled() != null) {
			registroOut.setCourier(registro.getScheduled().getIdCourier());
			registroOut.setScheduledDt(registro.getScheduled().getScheduledDate());
			registroOut.setComment(registro.getScheduled().getComment());
		} else
			registroOut.setScheduledDt(null);
		return registroOut;
	}
}
