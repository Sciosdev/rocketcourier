package com.rocket.service.model;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import com.rocket.service.entity.RegistryDto;



public class RegistroServiceInDto {	
	private List<RegistryDto> registro;
	@NotEmpty
	private String idVendor;
	private Integer tipoCarga;

	public List<RegistryDto> getRegistro() {
		return registro;
	}

	public void setRegistro(List<RegistryDto> registro) {
		this.registro = registro;
	}

	public String getIdVendor() {
		return idVendor;
	}

	public void setIdVendor(String idVendor) {
		this.idVendor = idVendor;
	}
	

    /**
     * @return String return the tipoCarga
     */
    public Integer getTipoCarga() {
        return tipoCarga;
    }

    /**
     * @param tipoCarga the tipoCarga to set
     */
    public void setTipoCarga(Integer tipoCarga) {
        this.tipoCarga = tipoCarga;
    }

}