import { Component, OnInit, ViewChild } from '@angular/core';
import { NbAuthOAuth2JWTToken, NbAuthService } from '@nebular/auth';
import { NbStepperComponent, NbToastrService } from '@nebular/theme';
import { PrevisualizacionComponent } from './previsualizacion/previsualizacion.component';
import { ShopifyService } from '../../services/shopify.service';
import { RegistroService } from '../../services/registro.service';

@Component({
  selector: 'app-carga-layout',
  templateUrl: './carga-layout.component.html',
  styleUrls: ['./carga-layout.component.scss']
})
export class CargaLayoutComponent implements OnInit {

  @ViewChild(PrevisualizacionComponent)
  previsualizacion: PrevisualizacionComponent; // Aunque no se use en el flujo de API directo, se mantiene por si hay otros usos (CSV)

  @ViewChild('stepper')
  stepper: NbStepperComponent;

  user: any;

  fechaInicio: string;
  fechaFin: string;

  archivoCargado:any;
  procesado:boolean = false; // Inicializar procesado
  tipoCarga: number;
  resultado: any;
  errores: any[] = []; // Inicializar errores

  constructor(
    private shopifyService: ShopifyService,
    private authService: NbAuthService,
    private registroService: RegistroService,
    private toastrService: NbToastrService
  ) { }

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

  setArchivoCargado($event: any) {
    this.archivoCargado = $event;
    // Si se carga un archivo, el tipoCarga podría ser diferente de 0 (Shopify API)
    // Esta lógica se maneja en el componente app-carga-archivo que emite (tipoCarga)
  }

  setProcesado($event: boolean) {
    this.procesado = $event;
  }

  setResultado($event: any) {
    this.resultado = $event;
    if ($event && $event.respuesta) {
        if ($event.registrosFallidos > 0 || ($event.registrosOmitidos > 0 && $event.registrosExitosos === 0 && $event.registrosFallidos === 0)) {
            this.errores = typeof $event.respuesta === 'string' ? $event.respuesta.split('|').map((err: string) => err.trim()) : [];
        } else {
            this.errores = [];
        }
    } else {
        this.errores = [];
    }
  }

  setErrores($event: any[]) {
    this.errores = $event;
  }

  setTipoCarga($event: number) {
    this.tipoCarga = $event;
    // Si el tipo de carga es CSV (ej. 1), el flujo del stepper es el original.
    // Si es Shopify API (ej. 0), se manejará en cargarMisEnvios.
  }

  cargarMisEnvios() { // Este método es para la carga desde la API de Shopify
    if (!this.user) {
      this.toastrService.show('No se pudo obtener la información del usuario.', 'Error', { status: 'danger', duration: 5000 });
      return;
    }
    if (!this.fechaInicio || !this.fechaFin) {
      this.toastrService.show('Por favor, seleccione fecha de inicio y fin.', 'Advertencia', { status: 'warning', duration: 5000 });
      return;
    }

    const vendor = this.user.user_name || this.user.name || this.user.sub; // Ajustar según cómo se almacena el username
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

    console.log(`[Shopify API Carga] Solicitando pedidos para vendor: ${vendor}, inicio: ${inicioISO}, fin: ${finISO}`);
    this.toastrService.info('Obteniendo pedidos de Shopify...', 'Procesando', { duration: 3000 });
    this.procesado = true;
    this.tipoCarga = 0; // Identificador para carga por API
    this.resultado = null; // Limpiar resultados anteriores
    this.errores = [];     // Limpiar errores anteriores


    this.shopifyService.obtenerOrders(vendor, inicioISO, finISO).subscribe(
      (payloadShopify: any) => {
        console.log('[Shopify API Carga] Respuesta de shopifyService.obtenerOrders:', JSON.stringify(payloadShopify));

        if (payloadShopify && typeof payloadShopify.registro !== 'undefined') {
          if (payloadShopify.registro.length > 0) {
            this.toastrService.info(`Se encontraron ${payloadShopify.registro.length} pedidos de Shopify. Registrando en la base de datos...`, 'Procesando', { duration: 3000 });

            this.registroService.registrarCarga(payloadShopify).subscribe(
              (resultadoCarga: any) => {
                this.setResultado(resultadoCarga);
                let toastMessage = `Carga API: ${resultadoCarga.registrosExitosos || 0} exitosos, ${resultadoCarga.registrosFallidos || 0} fallidos, ${resultadoCarga.registrosOmitidos || 0} omitidos.`;
                if (resultadoCarga.registrosFallidos > 0 || (resultadoCarga.registrosOmitidos > 0 && resultadoCarga.registrosExitosos === 0 && resultadoCarga.registrosFallidos === 0)) {
                    this.toastrService.warning(toastMessage, 'Resultado de Carga', { duration: 8000 });
                } else {
                    this.toastrService.success(toastMessage, 'Resultado de Carga', { duration: 5000 });
                }

                if (this.stepper) {
                  this.stepper.selectedIndex = 2;
                }
                this.procesado = false;
              },
              (errorRegistro: any) => {
                console.error('[Shopify API Carga] Error en registroService.registrarCarga:', errorRegistro);
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
                this.setResultado(this.resultado); // Para actualizar los errores
                if (this.stepper) {
                  this.stepper.selectedIndex = 2;
                }
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
            this.setResultado(this.resultado); // Para actualizar los errores
            if (this.stepper) {
              this.stepper.selectedIndex = 2;
            }
            this.procesado = false;
          }
        } else {
          console.error('[Shopify API Carga] La respuesta de obtenerOrders no tiene la estructura esperada:', payloadShopify);
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
          this.setResultado(this.resultado); // Para actualizar los errores
          if (this.stepper) {
            this.stepper.selectedIndex = 2;
          }
          this.procesado = false;
        }
      },
      (errorObtenerOrders: any) => {
        console.error('[Shopify API Carga] Error en shopifyService.obtenerOrders:', errorObtenerOrders);
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
        this.setResultado(this.resultado); // Para actualizar los errores
        if (this.stepper) {
            this.stepper.selectedIndex = 2;
        }
        this.procesado = false;
      }
    );
  }
}
