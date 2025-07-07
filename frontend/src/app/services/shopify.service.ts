import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ShopifyService {
  URL_SERVICIOS = environment.endpoint;

  constructor(private http: HttpClient) {}

  obtenerOrders(
    vendor: string | number,
    created_at_min?: string,
    created_at_max?: string
  ) {
    const url = this.URL_SERVICIOS + '/shopify/orders/' + vendor;
    let params = new HttpParams();
    if (created_at_min) {
      params = params.set('created_at_min', created_at_min);
    }
    if (created_at_max) {
      params = params.set('created_at_max', created_at_max);
    }
    const options = { headers: this.getHeaders(), params };
    return this.http.get(url, options);
  }

  private getHeaders() {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Accepts: 'application/json',
    });
  }
}
