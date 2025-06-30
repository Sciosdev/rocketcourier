package com.rocket.service.model;

import java.util.List;

import com.rocket.service.entity.Billing_addressDto;
import com.rocket.service.entity.EstatusDto;
import com.rocket.service.entity.Shipping_addressDto;

public class EstatusLogServiceDto {

    private EstatusDto estatusActual;
    private Billing_addressDto  origen;
    private Shipping_addressDto  destino;
    private List<HistoricoDto> historico;

    /**
     * @return EstatusDto return the estatusActual
     */
    public EstatusDto getEstatusActual() {
        return estatusActual;
    }

    /**
     * @param estatusActual the estatusActual to set
     */
    public void setEstatusActual(EstatusDto estatusActual) {
        this.estatusActual = estatusActual;
    }

    /**
     * @return List<HistoricoDto> return the historico
     */
    public List<HistoricoDto> getHistorico() {
        return historico;
    }

    /**
     * @param historico the historico to set
     */
    public void setHistorico(List<HistoricoDto> historico) {
        this.historico = historico;
    }


    /**
     * @return Shipping_addressDto return the destino
     */
    public Shipping_addressDto getDestino() {
        return destino;
    }

    /**
     * @param destino the destino to set
     */
    public void setDestino(Shipping_addressDto destino) {
        this.destino = destino;
    }


    /**
     * @return Billing_addressDto return the origen
     */
    public Billing_addressDto getOrigen() {
        return origen;
    }

    /**
     * @param origen the origen to set
     */
    public void setOrigen(Billing_addressDto origen) {
        this.origen = origen;
    }

}
