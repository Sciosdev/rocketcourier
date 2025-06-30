package com.rocket.service.model;

import com.rocket.service.entity.EstatusDto;

public class ActualizaEstatusServiceInDto {
    
    private EstatusDto estatusDto;
    private String orderKey;
    private String user;
    private String courier;
    private String comment;

    /**
     * @return EstatusDto return the estatusDto
     */
    public EstatusDto getEstatusDto() {
        return estatusDto;
    }

    /**
     * @param estatusDto the estatusDto to set
     */
    public void setEstatusDto(EstatusDto estatusDto) {
        this.estatusDto = estatusDto;
    }

    /**
     * @return String return the orderKey
     */
    public String getOrderKey() {
        return orderKey;
    }

    /**
     * @param orderKey the orderKey to set
     */
    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    /**
     * @return String return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }


    /**
     * @return String return the courier
     */
    public String getCourier() {
        return courier;
    }

    /**
     * @param courier the courier to set
     */
    public void setCourier(String courier) {
        this.courier = courier;
    }


    /**
     * @return String return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

}
