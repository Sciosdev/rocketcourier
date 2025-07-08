package com.rocket.service.mapper;

import java.util.Base64;

import com.rocket.service.entity.VendorDto;
import com.rocket.service.model.VendorCatalogServiceOutDto;
import com.rocket.service.model.VendorServiceDto;

import org.bson.types.Binary;

public class VendorMapper {

    public static VendorServiceDto mapVendorDtoToVendorServiceDto(VendorDto vendorDto) {

        VendorServiceDto vendorServiceOutDto = new VendorServiceDto();

        vendorServiceOutDto.setId(vendorDto.getId());
        vendorServiceOutDto.setNombreTienda(vendorDto.getNombreTienda());
        vendorServiceOutDto.setRutRazonSocial(vendorDto.getRutRazonSocial());
        vendorServiceOutDto.setGiroComercial(vendorDto.getGiroComercial());
        vendorServiceOutDto.setTipoProducto(vendorDto.getTipoProducto());
        vendorServiceOutDto.setCanalVenta(vendorDto.getCanalVenta());
        vendorServiceOutDto.setPreferenciaPagoFactura(vendorDto.getPreferenciaPagoFactura());
        vendorServiceOutDto.setSitio(vendorDto.getSitio());
        vendorServiceOutDto.setEmail(vendorDto.getEmail());
        vendorServiceOutDto.setTelefono(vendorDto.getTelefono());
        vendorServiceOutDto.setShopifyAccessToken(vendorDto.getShopifyAccessToken());
        vendorServiceOutDto.setShopifyStoreUrl(vendorDto.getShopifyStoreUrl());
        vendorServiceOutDto.setActivo(vendorDto.isActivo());
        vendorServiceOutDto.setDireccionCompleta(vendorDto.getDireccionCompleta());
        vendorServiceOutDto.setDireccion(vendorDto.getDireccion());

        if (vendorDto.getLogo() != null)
            vendorServiceOutDto.setLogo(Base64.getEncoder().encodeToString(vendorDto.getLogo().getData()));

        return vendorServiceOutDto;
    }

    public static VendorDto mapVendorServiceDtoToVendorDto(VendorServiceDto vendorServiceDto) {

        VendorDto vendorDto = new VendorDto();

        vendorDto.setId(vendorServiceDto.getId());
        vendorDto.setNombreTienda(vendorServiceDto.getNombreTienda());
        vendorDto.setRutRazonSocial(vendorServiceDto.getRutRazonSocial());
        vendorDto.setGiroComercial(vendorServiceDto.getGiroComercial());
        vendorDto.setTipoProducto(vendorServiceDto.getTipoProducto());
        vendorDto.setCanalVenta(vendorServiceDto.getCanalVenta());
        vendorDto.setPreferenciaPagoFactura(vendorServiceDto.getPreferenciaPagoFactura());
        vendorDto.setSitio(vendorServiceDto.getSitio());
        vendorDto.setEmail(vendorServiceDto.getEmail());
        vendorDto.setTelefono(vendorServiceDto.getTelefono());
        vendorDto.setShopifyAccessToken(vendorServiceDto.getShopifyAccessToken());
        vendorDto.setShopifyStoreUrl(vendorServiceDto.getShopifyStoreUrl());
        vendorDto.setActivo(vendorServiceDto.isActivo());
        vendorDto.setDireccionCompleta(vendorServiceDto.getDireccionCompleta());
        if(vendorServiceDto.getDireccionCompleta() != null)
        vendorDto.setDireccion(vendorServiceDto.getDireccionCompleta().toString());

        if (vendorServiceDto.getLogo() != null) {
            Binary binary = new Binary(Base64.getDecoder().decode(vendorServiceDto.getLogo()));
            vendorDto.setLogo(binary);
        }

        return vendorDto;
    }

    public static VendorCatalogServiceOutDto mapVendorDtoToVendorCatalogServiceOutDto(VendorDto vendorDto) {

        VendorCatalogServiceOutDto vendorCatalogServiceOutDto = new VendorCatalogServiceOutDto();

        vendorCatalogServiceOutDto.setId(vendorDto.getId());
        vendorCatalogServiceOutDto.setNombreTienda(vendorDto.getNombreTienda());
       
        return vendorCatalogServiceOutDto;
    }
}
