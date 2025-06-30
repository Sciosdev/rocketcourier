import { Component, OnInit, ViewChild } from '@angular/core';
import { NbAuthOAuth2JWTToken, NbAuthService } from '@nebular/auth';
import { PrevisualizacionComponent } from './previsualizacion/previsualizacion.component';
import { ShopifyService } from '../../services/shopify.service';

@Component({
  selector: 'app-carga-layout',
  templateUrl: './carga-layout.component.html',
  styleUrls: ['./carga-layout.component.scss']
})
export class CargaLayoutComponent implements OnInit {

  @ViewChild(PrevisualizacionComponent)
  previsualizacion: PrevisualizacionComponent;

  user: any;

  constructor(private shopifyService: ShopifyService, private authService: NbAuthService) { }

  ngOnInit(): void {
    this.authService.onTokenChange().subscribe((token: NbAuthOAuth2JWTToken) => {
      if (token.isValid()) {
        this.user = token.getAccessTokenPayload();
      }
    });
  }

  archivoCargado:any;
  procesado:boolean;
  tipoCarga: number;
  resultado: any;
  errores: any[];

  setArchivoCargado($event) {
    this.archivoCargado = $event;
  }

  setProcesado($event) {
    this.procesado = $event;
  }

  setResultado($event) {
    this.resultado = $event;
  }

  setErrores($event) {
    this.errores = $event;
  }

  setTipoCarga($event) {
    this.tipoCarga = $event;
  }

  cargarMisEnvios() {
    if (!this.user) {
      return;
    }
    const vendor = this.user.user_name || this.user;
    this.shopifyService.obtenerOrders(vendor).subscribe((data: any) => {
      if (this.previsualizacion) {
        this.previsualizacion.registerTest(JSON.stringify(data));
      }
    });
  }
}
