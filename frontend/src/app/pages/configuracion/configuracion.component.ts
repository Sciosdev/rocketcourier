import { Component, OnInit } from '@angular/core';
import { TiendaService } from '../../services/tienda.service';

@Component({
  selector: 'app-configuracion',
  templateUrl: './configuracion.component.html',
  styleUrls: ['./configuracion.component.scss']
})
export class ConfiguracionComponent implements OnInit {
  apiKey: string = '';
  accessToken: string = '';
  saving = false;

  constructor(private tiendaService: TiendaService) {}

  ngOnInit(): void {}

  guardar() {
    this.saving = true;
    const cred = {
      shopifyApiKey: this.apiKey,
      shopifyAccessToken: this.accessToken
    };
    // Por simplicidad se utiliza el id 1 en este ejemplo. En un caso real podría obtenerse de otra fuente
    this.tiendaService.actualizarCredencialesShopify(1, cred).subscribe(
      () => (this.saving = false),
      () => (this.saving = false)
    );
  }
}
