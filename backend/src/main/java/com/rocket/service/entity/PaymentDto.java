package com.rocket.service.entity;

import java.util.Date;

import javax.validation.constraints.NotNull;

public class PaymentDto {
	
	@NotNull(message = "paid_at Es un campo requerido")
	private Date paid_at;
	
	@NotNull(message = "total Es un campo requerido")
	private double total;
	private String payment_method;
	private String payment_reference;
	
	
	public Date getPaid_at() {
		return paid_at;
	}
	public void setPaid_at(Date paid_at) {
		this.paid_at = paid_at;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public String getPayment_method() {
		return payment_method;
	}
	public void setPayment_method(String payment_method) {
		this.payment_method = payment_method;
	}
	public String getPayment_reference() {
		return payment_reference;
	}
	public void setPayment_reference(String payment_reference) {
		this.payment_reference = payment_reference;
	}
		
}
