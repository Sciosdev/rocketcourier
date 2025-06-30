package com.rocket.service.entity;

import javax.persistence.GeneratedValue;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "TIENDA")
public class VendorDto {

	@Transient
    public static final String SEQUENCE_NAME = "TIENDA";
	
	@Id
	@GeneratedValue
	private Long id;
	private String nombreTienda;
	private String rutRazonSocial;
	private String giroComercial;
	private String direccion;
    private FullAddressDto direccionCompleta;
	private String tipoProducto;
	private String canalVenta;
	private String preferenciaPagoFactura;
	private String sitio;
	private String email;
	private String telefono;
	private Binary logo;
    private Boolean activo;

    /**
     * @return Long return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return String return the nombreTienda
     */
    public String getNombreTienda() {
        return nombreTienda;
    }

    /**
     * @param nombreTienda the nombreTienda to set
     */
    public void setNombreTienda(String nombreTienda) {
        this.nombreTienda = nombreTienda;
    }

    /**
     * @return String return the rutRazonSocial
     */
    public String getRutRazonSocial() {
        return rutRazonSocial;
    }

    /**
     * @param rutRazonSocial the rutRazonSocial to set
     */
    public void setRutRazonSocial(String rutRazonSocial) {
        this.rutRazonSocial = rutRazonSocial;
    }

    /**
     * @return String return the giroComercial
     */
    public String getGiroComercial() {
        return giroComercial;
    }

    /**
     * @param giroComercial the giroComercial to set
     */
    public void setGiroComercial(String giroComercial) {
        this.giroComercial = giroComercial;
    }

    /**
     * @return String return the direccion
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * @param direccion the direccion to set
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    /**
     * @return String return the tipoProducto
     */
    public String getTipoProducto() {
        return tipoProducto;
    }

    /**
     * @param tipoProducto the tipoProducto to set
     */
    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    /**
     * @return String return the canalVenta
     */
    public String getCanalVenta() {
        return canalVenta;
    }

    /**
     * @param canalVenta the canalVenta to set
     */
    public void setCanalVenta(String canalVenta) {
        this.canalVenta = canalVenta;
    }

    /**
     * @return String return the preferenciaPagoFactura
     */
    public String getPreferenciaPagoFactura() {
        return preferenciaPagoFactura;
    }

    /**
     * @param preferenciaPagoFactura the preferenciaPagoFactura to set
     */
    public void setPreferenciaPagoFactura(String preferenciaPagoFactura) {
        this.preferenciaPagoFactura = preferenciaPagoFactura;
    }

    /**
     * @return String return the sitio
     */
    public String getSitio() {
        return sitio;
    }

    /**
     * @param sitio the sitio to set
     */
    public void setSitio(String sitio) {
        this.sitio = sitio;
    }

    /**
     * @return String return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return String return the telefono
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @param telefono the telefono to set
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * @return Binary return the logo
     */
    public Binary getLogo() {
        return logo;
    }

    /**
     * @param logo the logo to set
     */
    public void setLogo(Binary logo) {
        this.logo = logo;
    }


    /**
     * @return Boolean return the activo
     */
    public Boolean isActivo() {
        return activo;
    }

    /**
     * @param activo the activo to set
     */
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }


    /**
     * @return FullAddressDto return the direccionCompleta
     */
    public FullAddressDto getDireccionCompleta() {
        return direccionCompleta;
    }

    /**
     * @param direccionCompleta the direccionCompleta to set
     */
    public void setDireccionCompleta(FullAddressDto direccionCompleta) {
        this.direccionCompleta = direccionCompleta;
    }

}