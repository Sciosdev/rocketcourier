package com.rocket.service.model;

import java.util.List;

public class ActualizaEstatusOrderMultipleServiceInDto {
    
    private Integer estatusId;
    private List<String> orderKey;
    private String user;
    private String courier;
    private String comment;

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


    /**
     * @return List<String> return the orderKey
     */
    public List<String> getOrderKey() {
        return orderKey;
    }

    /**
     * @param orderKey the orderKey to set
     */
    public void setOrderKey(List<String> orderKey) {
        this.orderKey = orderKey;
    }


    /**
     * @return Integer return the estatusId
     */
    public Integer getEstatusId() {
        return estatusId;
    }

    /**
     * @param estatusId the estatusId to set
     */
    public void setEstatusId(Integer estatusId) {
        this.estatusId = estatusId;
    }

}
