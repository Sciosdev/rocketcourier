package com.rocket.service.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "REGISTROS")
public class RegistryDto {

	@Transient
	public static final String SEQUENCE_NAME = "REGISTROS";

	@Id
	private Long id;
	private String rowNumber;
	private Long idCarga;
	private Integer idEstatus;
	private OrderDto order;
	private Billing_addressDto billing_address;
	private Shipping_addressDto shipping_address;
	private PaymentDto payment;
	private ExtraDto extra;
	private ScheduledDto scheduled;
	private List<EstatusLogDto> estatusLog;
	private String deliveryComment;
	private Integer deliveryException;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(String rowNumber) {
		this.rowNumber = rowNumber;
	}

	public Long getIdCarga() {
		return idCarga;
	}

	public void setIdCarga(Long idCarga) {
		this.idCarga = idCarga;
	}

	public OrderDto getOrder() {
		return order;
	}

	public void setOrder(OrderDto order) {
		this.order = order;
	}

	public Billing_addressDto getBilling_address() {
		return billing_address;
	}

	public void setBilling_address(Billing_addressDto billing_address) {
		this.billing_address = billing_address;
	}

	public Shipping_addressDto getShipping_address() {
		return shipping_address;
	}

	public void setShipping_address(Shipping_addressDto shipping_address) {
		this.shipping_address = shipping_address;
	}

	public PaymentDto getPayment() {
		return payment;
	}

	public void setPayment(PaymentDto payment) {
		this.payment = payment;
	}

	public ExtraDto getExtra() {
		return extra;
	}

	public void setExtra(ExtraDto extra) {
		this.extra = extra;
	}

	public Integer getIdEstatus() {
		return idEstatus;
	}

	public void setIdEstatus(Integer idEstatus) {
		this.idEstatus = idEstatus;
	}

	public ScheduledDto getScheduled() {
		return scheduled;
	}

	public void setScheduled(ScheduledDto scheduled) {
		this.scheduled = scheduled;
	}

	public List<EstatusLogDto> getEstatusLog() {
		return estatusLog;
	}

	public void setEstatusLog(List<EstatusLogDto> estatusLog) {
		this.estatusLog = estatusLog;
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


    /**
     * @return Integer return the deliveryException
     */
    public Integer getDeliveryException() {
        return deliveryException;
    }

    /**
     * @param deliveryException the deliveryException to set
     */
    public void setDeliveryException(Integer deliveryException) {
        this.deliveryException = deliveryException;
    }

}
