package com.rocket.service.model;

import java.util.Date;

public class EstatusLogDataDto implements Comparable<EstatusLogDataDto>{

    private String estatus;
    private String tipoEstatus;
    private Date fecha;
    private String usuario;

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
     * @return Date return the fecha
     */
    public Date getFecha() {
        return fecha;
    }

    /**
     * @param fecha the fecha to set
     */
    public void setFecha(Date fecha) {
        this.fecha = fecha;
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
     * @return String return the tipoEstatus
     */
    public String getTipoEstatus() {
        return tipoEstatus;
    }

    /**
     * @param tipoEstatus the tipoEstatus to set
     */
    public void setTipoEstatus(String tipoEstatus) {
        this.tipoEstatus = tipoEstatus;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((estatus == null) ? 0 : estatus.hashCode());
        result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
        result = prime * result + ((tipoEstatus == null) ? 0 : tipoEstatus.hashCode());
        result = prime * result + ((usuario == null) ? 0 : usuario.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EstatusLogDataDto other = (EstatusLogDataDto) obj;
        if (estatus == null) {
            if (other.estatus != null)
                return false;
        } else if (!estatus.equals(other.estatus))
            return false;
        if (tipoEstatus == null) {
            if (other.tipoEstatus != null)
                return false;
        } else if (!tipoEstatus.equals(other.tipoEstatus))
            return false;
        if (usuario == null) {
            if (other.usuario != null)
                return false;
        } else if (!usuario.equals(other.usuario))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EstatusLogDataDto [estatus=" + estatus + ", fecha=" + fecha + ", tipoEstatus=" + tipoEstatus
                + ", usuario=" + usuario + "]";
    }

    @Override
    public int compareTo(EstatusLogDataDto o) {
        return getFecha().compareTo(o.getFecha());
    }

}
