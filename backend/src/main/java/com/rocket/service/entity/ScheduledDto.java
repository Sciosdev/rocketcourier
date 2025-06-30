package com.rocket.service.entity;

import java.util.Date;

public class ScheduledDto {

	private Date scheduledDate;
	private String comment;
	private Boolean accepted;
	private String idVendor;
	private String idCourier;

	public String getIdVendor() {
		return idVendor;
	}

	public void setIdVendor(String idVendor) {
		this.idVendor = idVendor;
	}

	public Date getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(Date scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public Boolean getAccepted() {
		return accepted;
	}

	public void setAccepted(Boolean accepted) {
		this.accepted = accepted;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getIdCourier() {
		return idCourier;
	}

	public void setIdCourier(String idCourier) {
		this.idCourier = idCourier;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScheduledDto [scheduledDate=").append(scheduledDate).append(", comment=").append(comment)
				.append(", accepted=").append(accepted).append(", idVendor=").append(idVendor).append(", idCourier=")
				.append(idCourier).append("]");
		return builder.toString();
	}
	
}
