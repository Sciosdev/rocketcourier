package com.rocket.service.model;

import java.util.List;

public class GroupingEstatusRegistroServiceOutDto {

    private Integer id;
    private String estatus;
    private String tipo;
    private Integer total;
    private List<RegistroServiceOutDto> data;

    /**
     * @return String return the estatus
     */
    public String getEstatus() {
        return estatus;
    }

    /**
     * @param estatus the estatus to set
     */
    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    /**
     * @return Integer return the total
     */
    public Integer getTotal() {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(Integer total) {
        this.total = total;
    }

    /**
     * @return List<RegistroServiceOutDto> return the data
     */
    public List<RegistroServiceOutDto> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(List<RegistroServiceOutDto> data) {
        this.data = data;
    }

    /**
     * @return Integer return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return String return the tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

}
