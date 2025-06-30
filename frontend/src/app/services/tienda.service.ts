import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Tienda } from '../models/tienda.model';

@Injectable({
  providedIn: 'root'
})
export class TiendaService {

  constructor(public http: HttpClient) { }

  URL_SERVICIOS = environment.endpoint;
  
  obtenerTiendas() {

    const url = this.URL_SERVICIOS + '/vendor/';
    return this.http.get(url, this.getOptions());
  }

  obtenerCatalogoTiendas() {

    const url = this.URL_SERVICIOS + '/vendor-catalog/';
    return this.http.get(url, this.getOptions());
  }

  eliminarTienda(idTienda: number) {
    const url = this.URL_SERVICIOS + '/vendor/' + idTienda;
    return this.http.delete(url, this.getOptions());
  }

  actualizarTienda(tienda: Tienda) {

    const url = this.URL_SERVICIOS + '/vendor/';
    return this.http.put(url, tienda, this.getOptions());
  }


  crearTienda(tienda: Tienda) {

    const url = this.URL_SERVICIOS + '/vendor/';
    return this.http.post(url, tienda, this.getOptions());
  }


  private getOptions() {

    let headers = this.getHeaders();
    let options = { headers: headers };
    return options;
  }

  private getHeaders() {

    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accepts': 'application/json'
    });

    return headers;
  }
}
