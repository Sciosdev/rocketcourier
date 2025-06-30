package com.rocket.service.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistoricoDto implements Comparable<HistoricoDto>{

    private LocalDate fecha;
    private List<EstatusLogDataDto> log;

    public HistoricoDto(){
        this.fecha = LocalDate.now();
        this.log = new ArrayList<>();
    };

    public HistoricoDto(LocalDate fecha, List<EstatusLogDataDto> log) {
        this.fecha = fecha;
        this.log = log;
    }

    /**
     * @return LocalDate return the fecha
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * @param fecha the fecha to set
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * @return List<EstatusLogDataDto> return the log
     */
    public List<EstatusLogDataDto> getLog() {
        return log;
    }

    /**
     * @param log the log to set
     */
    public void setLog(List<EstatusLogDataDto> log) {
        this.log = log;
    }

    @Override
    public int compareTo(HistoricoDto o) {
        return getFecha().compareTo(o.getFecha());
    }

}
