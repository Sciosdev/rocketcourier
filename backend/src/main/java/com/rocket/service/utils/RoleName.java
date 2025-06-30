package com.rocket.service.utils;

public enum RoleName {
	COURIER("Courier"),
	ROOT("Root"),
	ADMIN("Administrador Rocket"),
	CUSTOMER("Vendedor"),
	MESSENGER("Repartidor/Recolector");

	String nombre;
	RoleName(String nombre) {
		this.nombre = nombre;
	}
	
	public String getValue(){
		return nombre;
	}
}
