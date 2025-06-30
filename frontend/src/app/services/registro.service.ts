import {
  HttpClient,
  HttpHeaders,
  HttpParams,
  HttpParamsOptions,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NbTokenService, NbAuthService, NbAuthToken } from '@nebular/auth';
import { environment } from 'src/environments/environment';
import { Registro } from '../models/registro.model';
import { ScheduleServiceInDto } from '../models/ScheduleServiceInDto.model';

@Injectable({
  providedIn: 'root',
})
export class RegistroService {
  URL_SERVICIOS = environment.endpoint;

  constructor(
    public http: HttpClient,
    public tokenService: NbTokenService,
    public authService: NbAuthService
  ) { }

  obtenerEstatusLogRegistro(registerKey: string) {
    console.log(registerKey);
    if (registerKey == undefined || registerKey == null || registerKey.trim() == '') registerKey = 'no-register-key';

    const url = this.URL_SERVICIOS + '/guest/registro/' + registerKey + '/estatus-log';
    const options = { headers: this.getHeaders() };
    return this.http.get(url, options);
  }

  registrarCarga(data: any) {
    const url = this.URL_SERVICIOS + '/registro';
    const options = { headers: this.getHeaders() };
    return this.http.post(url, data, options);
  }

  obtenerComunas() {
    const url = this.URL_SERVICIOS + '/comuna/list/';
    const options = { headers: this.getHeaders() };
    return this.http.get(url, options);
  }

  obtenerRegistros(customer: any, estatus: number, courier: string) {
    const url = this.URL_SERVICIOS + '/registro/list';

    let httpParams: HttpParamsOptions;

    if (customer && courier)
      httpParams = {
        fromObject: {
          estatus: estatus.toString(),
          courier: courier,
          customer: customer,
        },
      } as HttpParamsOptions;
    else if (customer && !courier)
      httpParams = {
        fromObject: { estatus: estatus.toString(), customer: customer },
      } as HttpParamsOptions;
    else if (!customer && courier)
      httpParams = {
        fromObject: { estatus: estatus.toString(), courier: courier },
      } as HttpParamsOptions;
    else
      httpParams = {
        fromObject: { estatus: estatus.toString() },
      } as HttpParamsOptions;

    const options = {
      params: new HttpParams(httpParams),
      headers: this.getHeaders(),
    };
    return this.http.get(url, options);
  }

  obtenerRegistrosPorFecha(
    customer: any,
    fDate: Date,
    tDate: Date,
    estatus: number,
    courier: string
  ) {
    const url = this.URL_SERVICIOS + '/registro/list';

    let httpParams: HttpParamsOptions;

    if (customer && courier)
      httpParams = {
        fromObject: {
          from: fDate.toDateString(),
          to: tDate.toDateString(),
          estatus: estatus.toString(),
          courier: courier,
          customer: customer,
        },
      } as HttpParamsOptions;
    else if (customer && !courier)
      httpParams = {
        fromObject: {
          from: fDate.toDateString(),
          to: tDate.toDateString(),
          estatus: estatus.toString(),
          customer: customer,
        },
      } as HttpParamsOptions;
    else if (!customer && courier)
      httpParams = {
        fromObject: {
          from: fDate.toDateString(),
          to: tDate.toDateString(),
          estatus: estatus.toString(),
          courier: courier,
        },
      } as HttpParamsOptions;
    else
      httpParams = {
        fromObject: {
          from: fDate.toDateString(),
          to: tDate.toDateString(),
          estatus: estatus.toString(),
        },
      } as HttpParamsOptions;

    const options = {
      params: new HttpParams(httpParams),
      headers: this.getHeaders(),
    };
    return this.http.get(url, options);
  }

  obtenerRegistrosPorIds(registroIds: any) {
    const url = this.URL_SERVICIOS + '/registro/list/';

    const httpParams: HttpParamsOptions = {
      fromObject: { registroIds: registroIds },
    } as HttpParamsOptions;
    const options = {
      params: new HttpParams(httpParams),
      headers: this.getHeaders(),
    };
    return this.http.get(url, options);
  }

  obtenerRegistroPorId(registroId: String) {
    const url = this.URL_SERVICIOS + '/registro/' + registroId;
    const options = { headers: this.getHeaders() };
    return this.http.get(url, options);
  }

  solicitarAgenda(data: ScheduleServiceInDto[]) {
    const url = this.URL_SERVICIOS + '/registro/agenda/solicitar';
    const options = { headers: this.getHeaders() };
    return this.http.put(url, data, options);
  }

  aceptarAgenda(data: ScheduleServiceInDto[]) {
    const url = this.URL_SERVICIOS + '/registro/agenda/aceptar';
    const options = { headers: this.getHeaders() };
    return this.http.put(url, data, options);
  }

  rechazarAgenda(data: ScheduleServiceInDto[]) {
    const url = this.URL_SERVICIOS + '/registro/agenda/rechazar';
    const options = { headers: this.getHeaders() };
    return this.http.put(url, data, options);
  }

  obtenerEtiqueta(orderKey: String) {
    const url = this.URL_SERVICIOS + '/api/registro/' + orderKey + '/etiqueta';

    let headers = new HttpHeaders({
      'Content-Type': 'application/pdf',
      Accepts: 'application/pdf',
    });

    return this.http.get(url, { responseType: 'blob' });
  }

  obtenerEtiquetaZip(orderKeys: any) {
    const url = this.URL_SERVICIOS + '/api/registro/etiqueta';

    let headers = new HttpHeaders({
      'Content-Type': 'application/pdf',
      Accepts: 'application/pdf',
    });

    const httpParams: HttpParamsOptions = {
      fromObject: { orderkeys: orderKeys },
    } as HttpParamsOptions;

    return this.http.get(url, {
      params: new HttpParams(httpParams),
      headers: headers,
      responseType: "blob"
    });
  }

  private getHeaders() {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accepts: 'application/json',
    });

    return headers;
  }
}
