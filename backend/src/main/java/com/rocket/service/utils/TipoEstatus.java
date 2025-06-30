package com.rocket.service.utils;

public enum TipoEstatus {
	
	INICIAL("inicial"),
	FINAL("final"),
	PROCESO("proceso"),
	REASIGNACION("reasignacion"),
	EXCEPCION("excepcion");

	String tipoDesc;
	TipoEstatus(String tipo) {
		this.tipoDesc = tipo;
	}
	
	public String getDescripcion(){
		return tipoDesc;
	}
}
