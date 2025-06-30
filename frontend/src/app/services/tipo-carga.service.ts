import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TipoCargaService {

  constructor(public http: HttpClient) { }

  URL_SERVICIOS = environment.endpoint;

  obtenerTipoCarga() {

    const url = this.URL_SERVICIOS + '/tipo-carga/';
    return this.http.get(url, this.getOptions());
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
