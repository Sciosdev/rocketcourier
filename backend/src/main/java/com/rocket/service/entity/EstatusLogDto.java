package com.rocket.service.entity;

import java.util.Date;

public class EstatusLogDto {

	private Integer estatusAnterior;
	private Integer estatusActual;
	private Date fechaActualizacion;
	private String usuario;

	public Integer getEstatusAnterior() {
		return estatusAnterior;
	}

	public void setEstatusAnterior(Integer estatusAnterior) {
		this.estatusAnterior = estatusAnterior;
	}

	public Integer getEstatusActual() {
		return estatusActual;
	}

	public void setEstatusActual(Integer estatusActual) {
		this.estatusActual = estatusActual;
	}

	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

}
