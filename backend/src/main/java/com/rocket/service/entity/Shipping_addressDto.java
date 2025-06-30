package com.rocket.service.entity;

import javax.validation.constraints.NotEmpty;

public class Shipping_addressDto {
	
	@NotEmpty(message = "name es un campo requerido")
	private String name;
	@NotEmpty(message = "street es un campo requerido")
	private String street;
	@NotEmpty(message = "address1 es un campo requerido")
	private String address1;
	@NotEmpty(message = "address2 es un campo requerido")
	private String address2;
	@NotEmpty(message = "company es un campo requerido")
	private String company;
	@NotEmpty(message = "city es un campo requerido")
	private String city;
	@NotEmpty(message = "zip es un campo requerido")
	private String zip;
	@NotEmpty(message = "province es un campo requerido")
	private String province;
	@NotEmpty(message = "province_name es un campo requerido")
	private String province_name;
	@NotEmpty(message = "country es un campo requerido")
	private String country;
	@NotEmpty(message = "phone es un campo requerido")
	private String phone;

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getAddress1() {
		return address1;
	}
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	public String getAddress2() {
		return address2;
	}
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getProvince_name() {
		return province_name;
	}
	public void setProvince_name(String province_name) {
		this.province_name = province_name;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
		
}
