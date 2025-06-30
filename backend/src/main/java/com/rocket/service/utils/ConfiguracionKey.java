package com.rocket.service.utils;

public enum ConfiguracionKey {

    ASIGNADO_AL_COURIER("c01"),
    EN_CURSO_DE_ENTREGA("c02");


    String key;

	ConfiguracionKey(String key) {
		this.key = key;
	}
	
	public String getKey(){
		return key;
	}
}
