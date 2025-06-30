import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Estatus } from '../models/estatus.model';

@Injectable({
  providedIn: 'root'
})
export class EstatusService {

  constructor(public http: HttpClient) { }

  URL_SERVICIOS = environment.endpoint;

  obtenerEstatus(username: string) {

    const url = this.URL_SERVICIOS + '/estatus/' + username;
    const options = { headers: this.getHeaders() };
    return this.http.get(url, options);

  }

  obtenerEstatusChange(username: string) {

    const url = this.URL_SERVICIOS + '/estatus-change/' + username;
    const options = { headers: this.getHeaders() };
    return this.http.get(url, options);

  }

  obtenerEstatusSiguiente(idEstatus: number) {

    const url = this.URL_SERVICIOS + '/estatus/' + idEstatus + '/siguiente/';
    const options = { headers: this.getHeaders() };
    return this.http.get(url, options);

  }

  
  obtenerEstatusSiguienteException(idEstatus: number) {

    const url = this.URL_SERVICIOS + '/estatus/' + idEstatus + '/siguiente-exception/';
    const options = { headers: this.getHeaders() };
    return this.http.get(url, options);

  }

  actualizarEstatus(estatusDto: Estatus, orderkey: string, user: string, courier: string) {
    const data = { estatusDto, orderKey: orderkey, user: user, courier: courier}; 
    const url = this.URL_SERVICIOS + '/estatus/';
    const options = { headers: this.getHeaders() };
    return this.http.put(url, data, options);
  }

  actualizarListaEstatus(estatusDto: Estatus, orderkeys: string[], user: string, courier: string, comment: string) {

    let body = [];
    orderkeys.forEach(orderKey => {
      let data;
      if(comment){
         data = { estatusDto, orderKey: orderKey, user: user, courier: courier, comment: comment}; 
      } else {
         data = { estatusDto, orderKey: orderKey, user: user, courier: courier}; 
      }

      body.push(data);
    })
    
    const url = this.URL_SERVICIOS + '/estatus/list';
    const options = { headers: this.getHeaders() };
    return this.http.put(url, body, options);
  }

  descartarListaEstatus(idEstatus: number, orderkeys: string[], user: string) {

    let body = [];
    orderkeys.forEach(orderKey => {
      let data = { idEstatus: idEstatus, orderKey: orderKey, user: user}; 
      body.push(data);
    })
    
    const url = this.URL_SERVICIOS + '/estatus/discard';
    const options = { headers: this.getHeaders() };
    return this.http.put(url, body, options);
  }


  private getHeaders() {

    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accepts': 'application/json'
    });
   
    return headers;
  }
}
