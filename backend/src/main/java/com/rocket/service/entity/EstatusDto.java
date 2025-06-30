package com.rocket.service.entity;

import javax.persistence.GeneratedValue;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ESTATUS")
public class EstatusDto {
	@Id
	@GeneratedValue
	private Integer id;
	private String desc;
	private String tipo;
	private Integer siguiente;
	private Integer siguienteExcepcion;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Integer getSiguiente() {
		return siguiente;
	}

	public void setSiguiente(Integer siguiente) {
		this.siguiente = siguiente;
	}

	public Integer getSiguienteExcepcion() {
		return siguienteExcepcion;
	}

	public void setSiguienteExcepcion(Integer siguienteExcepcion) {
		this.siguienteExcepcion = siguienteExcepcion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((siguiente == null) ? 0 : siguiente.hashCode());
		result = prime * result + ((siguienteExcepcion == null) ? 0 : siguienteExcepcion.hashCode());
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (getClass() != obj.getClass()) {
			if (((String) obj).equals(getDesc()))
				return true;
			else
				return false;
		}

		EstatusDto other = (EstatusDto) obj;
		if (desc == null) {
			if (other.desc != null)
				return false;
		} else if (!desc.equals(other.desc))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (siguiente == null) {
			if (other.siguiente != null)
				return false;
		} else if (!siguiente.equals(other.siguiente))
			return false;
		if (siguienteExcepcion == null) {
			if (other.siguienteExcepcion != null)
				return false;
		} else if (!siguienteExcepcion.equals(other.siguienteExcepcion))
			return false;
		if (tipo == null) {
			if (other.tipo != null)
				return false;
		} else if (!tipo.equals(other.tipo))
			return false;
		return true;
	}

}
