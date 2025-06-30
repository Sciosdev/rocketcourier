package com.rocket.service.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ROLES")
public class RolDto {

	private String id;
	private String rol;
	private String accessLevel;
	private List<Integer> statusView;
	private List<Integer> statusChange;
	private Boolean vendorAssignment;

	public RolDto(){
		this.id = "";
		this.rol = "";
		this.accessLevel = "";
		this.statusView = new ArrayList<>();
		this.statusChange = new ArrayList<>();
		this.vendorAssignment = false;
	}

	public RolDto(RolDto other){
		this.id = other.id;
		this.rol = other.rol;
		this.accessLevel = other.accessLevel;
		this.statusView = other.statusView;
		this.statusChange = other.statusChange;
		this.vendorAssignment = other.vendorAssignment;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public String getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(String accessLevel) {
		this.accessLevel = accessLevel;
	}

	public List<Integer> getStatusView() {
		return statusView;
	}

	public void setStatusView(List<Integer> statusView) {
		this.statusView = statusView;
	}

	/**
	 * @return List<Integer> return the statusChange
	 */
	public List<Integer> getStatusChange() {
		return statusChange;
	}

	/**
	 * @param statusChange the statusChange to set
	 */
	public void setStatusChange(List<Integer> statusChange) {
		this.statusChange = statusChange;
	}


    /**
     * @return Boolean return the vendorAssignment
     */
    public Boolean isVendorAssignment() {
        return vendorAssignment;
    }

    /**
     * @param vendorAssignment the vendorAssignment to set
     */
    public void setVendorAssignment(Boolean vendorAssignment) {
        this.vendorAssignment = vendorAssignment;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessLevel == null) ? 0 : accessLevel.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((rol == null) ? 0 : rol.hashCode());
		result = prime * result + ((statusChange == null) ? 0 : statusChange.hashCode());
		result = prime * result + ((statusView == null) ? 0 : statusView.hashCode());
		result = prime * result + ((vendorAssignment == null) ? 0 : vendorAssignment.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RolDto other = (RolDto) obj;
		if (accessLevel == null) {
			if (other.accessLevel != null)
				return false;
		} else if (!accessLevel.equals(other.accessLevel))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (rol == null) {
			if (other.rol != null)
				return false;
		} else if (!rol.equals(other.rol))
			return false;
		if (statusChange == null) {
			if (other.statusChange != null)
				return false;
		} else if (!statusChange.equals(other.statusChange))
			return false;
		if (statusView == null) {
			if (other.statusView != null)
				return false;
		} else if (!statusView.equals(other.statusView))
			return false;
		if (vendorAssignment == null) {
			if (other.vendorAssignment != null)
				return false;
		} else if (!vendorAssignment.equals(other.vendorAssignment))
			return false;
		return true;
	}

}
