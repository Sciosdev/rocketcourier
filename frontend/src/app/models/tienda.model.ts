import { FullAddress } from './address.model';
export class Tienda {
  id: number;
  nombreTienda: string;
  sitio?: string;
  rutRazonSocial?: string;
  giroComercial?: string;
  direccion?: string;
  direccionCompleta: FullAddress;
  tipoProducto?: string;
  canalVenta?: string;
  preferenciaPagoFactura?: string;
  email?: string;
  telefono?: string;
  shopifyAccessToken?: string;
  shopifyStoreUrl?: string;
  activo: boolean;
  logo?: any;

  constructor() {
   this.direccionCompleta = {};
  }

  setTienda(tienda: Tienda) {
    if (tienda.id) this.id = tienda.id;
    if (tienda.nombreTienda) this.nombreTienda = tienda.nombreTienda;
    if (tienda.sitio) this.sitio = tienda.sitio;
    if (tienda.rutRazonSocial) this.rutRazonSocial = tienda.rutRazonSocial;
    if (tienda.giroComercial) this.giroComercial = tienda.giroComercial;
    if (tienda.direccion) this.direccion = tienda.direccion;
    if (tienda.direccionCompleta) this.direccionCompleta = tienda.direccionCompleta;
    if (tienda.tipoProducto) this.tipoProducto = tienda.tipoProducto;
    if (tienda.canalVenta) this.canalVenta = tienda.canalVenta;
    if (tienda.preferenciaPagoFactura) this.preferenciaPagoFactura = tienda.preferenciaPagoFactura;
    if (tienda.email) this.email = tienda.email;
    if (tienda.telefono) this.telefono = tienda.telefono;
    if (tienda.shopifyAccessToken) this.shopifyAccessToken = tienda.shopifyAccessToken;
    if (tienda.shopifyStoreUrl) this.shopifyStoreUrl = tienda.shopifyStoreUrl;
    if (tienda.activo) this.activo = tienda.activo;
    if (tienda.logo) this.logo = tienda.logo;
  }
}
