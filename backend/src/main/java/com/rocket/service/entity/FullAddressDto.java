package com.rocket.service.entity;

public class FullAddressDto {
    
    private String street;
    private String internal;
    private String external;
    private String zipCode;
    private String commune;
    private String province;
    private String country;

    /**
     * @return String return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * @param street the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * @return String return the internal
     */
    public String getInternal() {
        return internal;
    }

    /**
     * @param internal the internal to set
     */
    public void setInternal(String internal) {
        this.internal = internal;
    }

    /**
     * @return String return the external
     */
    public String getExternal() {
        return external;
    }

    /**
     * @param external the external to set
     */
    public void setExternal(String external) {
        this.external = external;
    }

    /**
     * @return String return the zipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * @param zipCode the zipCode to set
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * @return String return the commune
     */
    public String getCommune() {
        return commune;
    }

    /**
     * @param commune the commune to set
     */
    public void setCommune(String commune) {
        this.commune = commune;
    }

    /**
     * @return String return the province
     */
    public String getProvince() {
        return province;
    }

    /**
     * @param province the province to set
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * @return String return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

		builder.append(getStreet());

		if (getExternal() != null && !getExternal().trim().isEmpty()) {
			builder.append(" ");
			builder.append(getExternal());
		}

		if (getInternal() != null && !getInternal().trim().isEmpty()) {
			builder.append("-");
			builder.append(getInternal());
		}

		if (getZipCode() != null && !getZipCode().trim().isEmpty()) {
			builder.append(", ");
			builder.append(getZipCode());
		}

		if (getCommune() != null && !getCommune().trim().isEmpty()) {
			builder.append(", ");
			builder.append(getCommune());
		}

		if (getProvince() != null && !getProvince().trim().isEmpty()) {
			builder.append(", ");
			builder.append(getProvince());
		}

		if (getCountry() != null && !getCountry().trim().isEmpty()) {
			builder.append(", ");
			builder.append(getCountry());
		}

		return builder.toString();

        
    }

}
