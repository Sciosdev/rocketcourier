import { Component, OnInit } from '@angular/core';
import { NbAuthOAuth2JWTToken, NbAuthService } from '@nebular/auth';
import { NbToastrService } from '@nebular/theme';
import { ShopifyService } from '../../services/shopify.service';
import { RegistroService } from '../../services/registro.service';

@Component({
  selector: 'app-carga-shopify',
  templateUrl: './carga-shopify.component.html',
  styleUrls: ['./carga-shopify.component.scss']
})
export class CargaShopifyComponent implements OnInit {
  user: any;
  fechaInicio: string;
  fechaFin: string;
  resultado: any;
  errores: any[] = [];
  procesado = false;

  constructor(
    private shopifyService: ShopifyService,
    private authService: NbAuthService,
    private registroService: RegistroService,
    private toastrService: NbToastrService
  ) {}

  ngOnInit(): void {
    this.authService.onTokenChange().subscribe((token: NbAuthOAuth2JWTToken) => {
      if (token.isValid()) {
        this.user = token.getAccessTokenPayload();
      }
    });

    const today = new Date();
    const year = today.getFullYear();
    const month = ('0' + (today.getMonth() + 1)).slice(-2);
    const day = ('0' + today.getDate()).slice(-2);
    this.fechaInicio = `${year}-${month}-${day}`;
    this.fechaFin = `${year}-${month}-${day}`;
  }

  private setResultado(res: any) {
    this.resultado = res;
    if (res && res.respuesta) {
      if (
        res.registrosFallidos > 0 ||
        (res.registrosOmitidos > 0 && res.registrosExitosos === 0 && res.registrosFallidos === 0)
      ) {
        this.errores = typeof res.respuesta === 'string' ? res.respuesta.split('|').map((e: string) => e.trim()) : [];
      } else {
        this.errores = [];
      }
    } else {
      this.errores = [];
    }
  }

  cargarMisEnvios() {
    if (!this.user) {
      this.toastrService.show('No se pudo obtener la información del usuario.', 'Error', { status: 'danger', duration: 5000 });
      return;
    }
    if (!this.fechaInicio || !this.fechaFin) {
      this.toastrService.show('Por favor, seleccione fecha de inicio y fin.', 'Advertencia', { status: 'warning', duration: 5000 });
      return;
    }

    const vendor = this.user.user_name || this.user.name || this.user.sub;
    if (!vendor) {
      this.toastrService.show('No se pudo determinar el vendor.', 'Error', { status: 'danger', duration: 5000 });
      return;
    }

    const startDate = new Date(this.fechaInicio);
    startDate.setUTCHours(0, 0, 0, 0);
    const inicioISO = startDate.toISOString();

    const endDate = new Date(this.fechaFin);
    endDate.setUTCHours(23, 59, 59, 999);
    const finISO = endDate.toISOString();

    this.toastrService.info('Obteniendo pedidos de Shopify...', 'Procesando', { duration: 3000 });
    this.procesado = true;
    this.resultado = null;
    this.errores = [];

    this.shopifyService.obtenerOrders(vendor, inicioISO, finISO).subscribe(
      (payloadShopify: any) => {
        if (payloadShopify && typeof payloadShopify.registro !== 'undefined') {
          if (payloadShopify.registro.length > 0) {
            this.toastrService.info(`Se encontraron ${payloadShopify.registro.length} pedidos de Shopify. Registrando en la base de datos...`, 'Procesando', { duration: 3000 });
            this.registroService.registrarCarga(payloadShopify).subscribe(
              (resultadoCarga: any) => {
                this.setResultado(resultadoCarga);
                const toastMessage = `Carga API: ${resultadoCarga.registrosExitosos || 0} exitosos, ${resultadoCarga.registrosFallidos || 0} fallidos, ${resultadoCarga.registrosOmitidos || 0} omitidos.`;
                if (resultadoCarga.registrosFallidos > 0 || (resultadoCarga.registrosOmitidos > 0 && resultadoCarga.registrosExitosos === 0 && resultadoCarga.registrosFallidos === 0)) {
                  this.toastrService.warning(toastMessage, 'Resultado de Carga', { duration: 8000 });
                } else {
                  this.toastrService.success(toastMessage, 'Resultado de Carga', { duration: 5000 });
                }
                this.procesado = false;
              },
              (errorRegistro: any) => {
                const detailError = errorRegistro.error?.respuesta || errorRegistro.error?.error || errorRegistro.error?.message || errorRegistro.message || 'Error desconocido al registrar.';
                this.toastrService.danger(`Error al registrar los pedidos: ${detailError}`, 'Error Fatal', { duration: 8000 });
                this.resultado = {
                  idCarga: null,
                  uploadDate: new Date().toISOString(),
                  registrosExitosos: 0,
                  registrosFallidos: payloadShopify.registro ? payloadShopify.registro.length : 0,
                  registrosOmitidos: 0,
                  respuesta: `Error al registrar pedidos: ${detailError}`,
                  idVendor: vendor,
                  tipoCarga: 0
                };
                this.setResultado(this.resultado);
                this.procesado = false;
              }
            );
          } else {
            this.toastrService.warning('No se encontraron pedidos de Shopify para las fechas seleccionadas.', 'Información', { duration: 5000 });
            this.resultado = {
              idCarga: null,
              uploadDate: new Date().toISOString(),
              registrosExitosos: 0,
              registrosFallidos: 0,
              registrosOmitidos: 0,
              respuesta: 'No se encontraron pedidos de Shopify para procesar.',
              idVendor: vendor,
              tipoCarga: 0
            };
            this.setResultado(this.resultado);
            this.procesado = false;
          }
        } else {
          this.toastrService.danger('Error al procesar la respuesta de Shopify: estructura inesperada.', 'Error', { duration: 5000 });
          this.resultado = {
            idCarga: null,
            uploadDate: new Date().toISOString(),
            registrosExitosos: 0,
            registrosFallidos: 0,
            registrosOmitidos: 0,
            respuesta: 'Error: Respuesta inesperada del servidor al obtener pedidos.',
            idVendor: vendor,
            tipoCarga: 0
          };
          this.setResultado(this.resultado);
          this.procesado = false;
        }
      },
      (errorObtenerOrders: any) => {
        const errorMessage = errorObtenerOrders.error?.responseMessage || errorObtenerOrders.error?.error || errorObtenerOrders.error?.message || 'Error desconocido al conectar con Shopify.';
        this.toastrService.danger(`Error al conectar con Shopify: ${errorMessage}`, 'Error', { duration: 8000 });
        this.resultado = {
          idCarga: null,
          uploadDate: new Date().toISOString(),
          registrosExitosos: 0,
          registrosFallidos: 0,
          registrosOmitidos: 0,
          respuesta: `Error al conectar con Shopify: ${errorMessage}`,
          idVendor: vendor,
          tipoCarga: 0
        };
        this.setResultado(this.resultado);
        this.procesado = false;
      }
    );
  }
}
