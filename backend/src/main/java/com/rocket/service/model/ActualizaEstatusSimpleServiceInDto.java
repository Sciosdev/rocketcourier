package com.rocket.service.model;

public class ActualizaEstatusSimpleServiceInDto {

    private Integer idEstatus;
    private String orderKey;
    private String usuario;
    private String deliveryComment;
    private Integer deliveryException;

    /**
     * @return Integer return the idEstatus
     */
    public Integer getIdEstatus() {
        return idEstatus;
    }

    /**
     * @param idEstatus the idEstatus to set
     */
    public void setIdEstatus(Integer idEstatus) {
        this.idEstatus = idEstatus;
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
     * @return String return the usuario
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(String usuario) {
        this.usuario = usuario;
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