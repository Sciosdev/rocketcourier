import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RolesService {
  URL_SERVICIOS = environment.endpoint;

  constructor(public http: HttpClient) { }

  obtenerRoles() {

    const url = this.URL_SERVICIOS + '/rol/';
    return this.http.get(url, this.getOptions());

  }

  obtenerRol(roleName: string) {

    const url = this.URL_SERVICIOS + '/rol/' + roleName;
    return this.http.get(url, this.getOptions());

  }

  private getOptions() {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accepts': 'application/json'
    });
    let options = { headers: headers };
    return options;
  }

}
