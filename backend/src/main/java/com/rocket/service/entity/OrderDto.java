package com.rocket.service.entity;

import java.util.Date;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

public class OrderDto {
	private String id;
	@BsonId
	private ObjectId orderKey;
	private String name;
	@Email
	private String email;
	@NotEmpty(message = "vendor es un campo requerido")
	private String vendor;
	@NotEmpty(message = "risk_level es un campo requerido")
	private String risk_level;
	@NotEmpty(message = "vendor es un campo requerido")
	private String source;
	@NotEmpty(message = "financial_status es un campo requerido")
	private String financial_status;
	@NotEmpty(message = "accepts_marketing es un campo requerido")
	private String accepts_marketing;
	@NotEmpty(message = "accepts_marketing es un campo requerido")
	private String currency;
	private double subtotal;	
	@NotEmpty(message = "shipping es un campo requerido")
	private double shipping;
	@NotEmpty(message = "shipping_method es un campo requerido")
	private String shipping_method;
	@NotNull(message = "created_at Es un campo requerido")
	private Date created_at;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getRisk_level() {
		return risk_level;
	}
	public void setRisk_level(String risk_level) {
		this.risk_level = risk_level;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getFinancial_status() {
		return financial_status;
	}
	public void setFinancial_status(String financial_status) {
		this.financial_status = financial_status;
	}
	public String getAccepts_marketing() {
		return accepts_marketing;
	}
	public void setAccepts_marketing(String accepts_marketing) {
		this.accepts_marketing = accepts_marketing;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public double getSubtotal() {
		return subtotal;
	}
	public void setSubtotal(double subtotal) {
		this.subtotal = subtotal;
	}
	public double getShipping() {
		return shipping;
	}
	public void setShipping(double shipping) {
		this.shipping = shipping;
	}
	public String getShipping_method() {
		return shipping_method;
	}
	public void setShipping_method(String shipping_method) {
		this.shipping_method = shipping_method;
	}
	public Date getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}
	public ObjectId getOrderKey() {
		return orderKey;
	}
	public void setOrderKey(ObjectId orderKey) {
		this.orderKey = orderKey;
	}
}
