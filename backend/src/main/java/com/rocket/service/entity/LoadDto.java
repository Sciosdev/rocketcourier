package com.rocket.service.entity;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "CARGA")
public class LoadDto {
	
	@Transient
    public static final String SEQUENCE_NAME = "CARGA";
	
	@Id
	@GeneratedValue
	private Long idCarga;
	@NotNull(message="uploadDate debe contener un valor")
	private Date uploadDate;
	private int registrosExitosos;
	private int registrosFallidos;
	private int registrosOmitidos;	
	private String respuesta;
	private String idVendor;
	private Integer tipoCarga;
		
	public Long getIdCarga() {
		return idCarga;
	}
	public void setIdCarga(Long idCarga) {
		this.idCarga = idCarga;
	}
	public int getRegistrosExitosos() {
		return registrosExitosos;
	}
	public void setRegistrosExitosos(int registrosExitosos) {
		this.registrosExitosos = registrosExitosos;
	}
	public int getRegistrosFallidos() {
		return registrosFallidos;
	}
	public void setRegistrosFallidos(int registrosFallidos) {
		this.registrosFallidos = registrosFallidos;
	}
	public int getRegistrosOmitidos() {
		return registrosOmitidos;
	}
	public void setRegistrosOmitidos(int registrosOmitidos) {
		this.registrosOmitidos = registrosOmitidos;
	}
	public Date getUploadDate() {
		return uploadDate;
	}
	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}
	public String getRespuesta() {
		return respuesta;
	}
	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}
	public String getIdVendor() {
		return idVendor;
	}
	public void setIdVendor(String idVendor) {
		this.idVendor = idVendor;
	}
	
			

    /**
     * @return Integer return the tipoCarga
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
