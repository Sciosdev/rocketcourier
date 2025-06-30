package com.rocket.service.model;

import java.util.Date;

public class RegistroServiceOutDto {
	private String orderkey;
	private String name;
	private String email;
	private String shippingCity;
	private String shippingAdress1;
	private String shippingAdress2;
	private String descStatus;
	private Date cargaDt;
	private Date scheduledDt;
	private String comment;
	private String courier;
	private String vendedor;
	private String deliveryComment;
	
	public String getOrderkey() {
		return orderkey;
	}
	public void setOrderkey(String orderkey) {
		this.orderkey = orderkey;
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
	
	public String getShippingCity() {
		return shippingCity;
	}
	public void setShippingCity(String shippingCity) {
		this.shippingCity = shippingCity;
	}
	public String getShippingAdress1() {
		return shippingAdress1;
	}
	public void setShippingAdress1(String shippingAdress1) {
		this.shippingAdress1 = shippingAdress1;
	}
	public String getShippingAdress2() {
		return shippingAdress2;
	}
	public void setShippingAdress2(String shippingAdress2) {
		this.shippingAdress2 = shippingAdress2;
	}
	public String getDescStatus() {
		return descStatus;
	}
	public void setDescStatus(String descStatus) {
		this.descStatus = descStatus;
	}
	public Date getCargaDt() {
		return cargaDt;
	}
	public void setCargaDt(Date cargaDt) {
		this.cargaDt = cargaDt;
	}
	public Date getScheduledDt() {
		return scheduledDt;
	}
	public void setScheduledDt(Date scheduledDt) {
		this.scheduledDt = scheduledDt;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getCourier() {
		return courier;
	}
	public void setCourier(String courier) {
		this.courier = courier;
	}
	public String getVendedor() {
		return vendedor;
	}
	public void setVendedor(String vendedor) {
		this.vendedor = vendedor;
	}
	
	

    /**
     * @return String return the deliveryComment
     */
    public String getDeliveryComment() {
        return deliveryComment;
    }

    /**
     * @param deliveryComment the deliveryComment to set
     */
    public void setDeliveryComment(String deliveryComment) {
        this.deliveryComment = deliveryComment;
    }

}
