import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpParamsOptions } from '@angular/common/http';
import { NbAuthService, NbAuthJWTToken, NbTokenService } from '@nebular/auth';
import { environment } from 'src/environments/environment';
import { UsuarioCompleto } from '../models/usuario-completo.model';
import { Usuario } from '../models/usuario.model';

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  URL_SERVICIOS = environment.endpoint;

  constructor(public http: HttpClient,
    public tokenService: NbTokenService,
    public authService: NbAuthService) { }


  obtenerRepartidores() {

    const url = this.URL_SERVICIOS + '/repartidor';
    return this.http.get(url, this.getOptions());

  }
  obtenerVendedores() {

    const url = this.URL_SERVICIOS + '/vendedor';
    return this.http.get(url, this.getOptions());

  }

  obtenerCouriers() {

    const url = this.URL_SERVICIOS + '/courier';
    return this.http.get(url, this.getOptions());

  }

  obtenerUsuario(username: string) {
    const url = this.URL_SERVICIOS + '/user/' + username;
    return this.http.get(url, this.getOptions());
  }

  obtenerUsuarioCompleto(username: string) {
    const url = this.URL_SERVICIOS + '/user-full/' + username;
    return this.http.get(url, this.getOptions());
  }

  obtenerUsuarios(rol: string, tienda: number) {

    const url = this.URL_SERVICIOS + '/user/';

    let httpParams: HttpParamsOptions;

    if(rol != undefined && tienda != undefined){
      httpParams = { fromObject: { rol: rol.toString(), tienda: tienda.toString()  } } as HttpParamsOptions;
    } else if (rol != undefined) {
      httpParams = { fromObject: { rol: rol.toString() } } as HttpParamsOptions;
    } else if (tienda != undefined) {
      httpParams = { fromObject: { tienda: tienda.toString() } } as HttpParamsOptions;
    } else {
      httpParams = { fromObject: {  } } as HttpParamsOptions;
    }

    const options = { params: new HttpParams(httpParams), headers: this.getHeaders() };
    return this.http.get(url, options);
  }

  eliminarUsuario(usuario:string) {
    const url = this.URL_SERVICIOS + '/user/' + usuario;
    return this.http.delete(url, this.getOptions());
  }

  actualizarUsuarios(usuario: UsuarioCompleto) {
    const url = this.URL_SERVICIOS + '/user/';
    const options = { headers: this.getHeaders() };
    return this.http.put(url, usuario, options);
  }

  agregarUsuario(usuario: UsuarioCompleto) {
    const url = this.URL_SERVICIOS + '/user/';
    const options = { headers: this.getHeaders() };
    return this.http.post(url, usuario, options);
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
