import { HttpClient, HttpHeaders } from '@angular/common/http';
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
    let url = this.URL_SERVICIOS + '/shopify/orders/' + vendor;
    const params = [] as string[];
    if (created_at_min) {
      params.push('created_at_min=' + encodeURIComponent(created_at_min));
    }
    if (created_at_max) {
      params.push('created_at_max=' + encodeURIComponent(created_at_max));
    }
    if (params.length) {
      url += '?' + params.join('&');
    }
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
