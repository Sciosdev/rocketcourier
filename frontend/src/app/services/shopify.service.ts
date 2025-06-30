import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ShopifyService {
  URL_SERVICIOS = environment.endpoint;

  constructor(private http: HttpClient) {}

  obtenerOrders(vendor: string | number) {
    const url = this.URL_SERVICIOS + '/shopify/orders/' + vendor;
    const options = { headers: this.getHeaders() };
    return this.http.get(url, options);
  }

  private getHeaders() {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Accepts: 'application/json',
    });
  }
}
