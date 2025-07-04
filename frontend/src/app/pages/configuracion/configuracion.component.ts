import { Component, OnInit } from '@angular/core';
import { NbAuthOAuth2JWTToken, NbAuthService } from '@nebular/auth';
import { NbToastrService } from '@nebular/theme';
import { TiendaService } from '../../services/tienda.service';
import { UsuarioService } from 'src/app/services/usuario.service';
import { UsuarioCompleto } from 'src/app/models/usuario-completo.model';

@Component({
  selector: 'app-configuracion',
  templateUrl: './configuracion.component.html',
  styleUrls: ['./configuracion.component.scss']
})
export class ConfiguracionComponent implements OnInit {
  apiKey: string = '';
  accessToken: string = '';
  storeUrl: string = '';
  saving = false;
  vendorId?: number;

  constructor(
    private tiendaService: TiendaService,
    private authService: NbAuthService,
    private usuarioService: UsuarioService,
    private toastrService: NbToastrService
  ) {}

  ngOnInit(): void {
    this.authService.getToken().subscribe((token: NbAuthOAuth2JWTToken) => {
      if (token.isValid()) {
        const payload: any = token.getAccessTokenPayload();
        const username = payload.user_name;
        this.usuarioService.obtenerUsuarioCompleto(username).subscribe(
          (user: UsuarioCompleto) => {
            this.vendorId = user.tienda;
            if (this.vendorId) {
              this.tiendaService
                .obtenerCredencialesShopify(this.vendorId)
                .subscribe(
                  (cred: any) => {
                    this.apiKey = cred.shopifyApiKey || '';
                    this.accessToken = cred.shopifyAccessToken || '';
                    this.storeUrl = cred.shopifyStoreUrl || '';
                  },
                  (err) => {
                    console.error(
                      'Error obteniendo credenciales de Shopify',
                      err
                    );
                  }
                );
            }
          },
          (err) => {
            console.error('No se pudo obtener el ID de la tienda', err);
            this.vendorId = undefined;
          }
        );
      }
    });
  }

  guardar() {
    if (!this.vendorId) {
      console.error('Vendor ID no disponible');
      return;
    }
    this.saving = true;
    const cred = {
      shopifyApiKey: this.apiKey,
      shopifyAccessToken: this.accessToken,
      shopifyStoreUrl: this.storeUrl
    };
    this.tiendaService
      .actualizarCredencialesShopify(this.vendorId, cred)
      .subscribe(
        () => {
          this.toastrService.success(
            'Credenciales actualizadas correctamente',
            'Actualización'
          );
          this.saving = false;
        },
        (err) => {
          console.error('Error actualizando credenciales', err);
          this.toastrService.danger(
            'No se pudieron actualizar las credenciales',
            'Actualización'
          );
          this.saving = false;
        }
      );
  }
}
