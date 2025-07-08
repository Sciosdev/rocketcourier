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
  previsualizacion: PrevisualizacionComponent;

  @ViewChild('stepper')
  stepper: NbStepperComponent;

  user: any;

  fechaInicio: string;
  fechaFin: string;

  archivoCargado:any;
  procesado:boolean;
  tipoCarga: number;
  resultado: any;
  errores: any[];

  constructor(
    private shopifyService: ShopifyService,
    private authService: NbAuthService,
    private registroService: RegistroService, // Inyectado para uso futuro si es necesario
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
  }

  setProcesado($event: boolean) {
    this.procesado = $event;
  }

  setResultado($event: any) {
    this.resultado = $event;
  }

  setErrores($event: any[]) {
    this.errores = $event;
  }

  setTipoCarga($event: number) {
    this.tipoCarga = $event;
  }

  cargarMisEnvios() {
    if (!this.user) {
      this.toastrService.show('No se pudo obtener la información del usuario.', 'Error', { status: 'danger' });
      return;
    }
    if (!this.fechaInicio || !this.fechaFin) {
      this.toastrService.show('Por favor, seleccione fecha de inicio y fin.', 'Advertencia', { status: 'warning' });
      return;
    }

    const vendor = this.user.user_name || this.user;

    const startDate = new Date(this.fechaInicio);
    startDate.setUTCHours(0, 0, 0, 0);
    const inicioISO = startDate.toISOString();

    const endDate = new Date(this.fechaFin);
    endDate.setUTCHours(23, 59, 59, 999);
    const finISO = endDate.toISOString();

    console.log(`Solicitando pedidos para vendor: ${vendor}, inicio: ${inicioISO}, fin: ${finISO}`);

    if (this.stepper) {
      this.stepper.selectedIndex = 1;
    }

    this.shopifyService.obtenerOrders(vendor, inicioISO, finISO).subscribe(
      (response: any) => { // 'response' ahora es el Map que contiene 'payload' y 'debugShopifyResponse'
        console.log('Respuesta completa de shopifyService.obtenerOrders:', JSON.stringify(response));

        if (response && response.debugShopifyResponse) {
          console.log('Datos de depuración de Shopify (respuesta cruda del primer pedido o mensaje de error):', response.debugShopifyResponse);
        }

        // Mover al paso de previsualización/resultados aquí, después de recibir la respuesta.
        if (this.stepper) {
            this.stepper.selectedIndex = 1;
        }

        const payloadData = response && response.payload ? response.payload : null; // Extraer el payload original

        if (payloadData && payloadData.registro) {
          if (payloadData.registro.length > 0) {
            this.toastrService.info(`Se encontraron ${payloadData.registro.length} pedidos de Shopify. Enviando para registro...`, 'Información', { duration: 5000 });
          } else {
            this.toastrService.warning('No se encontraron pedidos de Shopify para las fechas seleccionadas o la respuesta de Shopify estaba vacía.', 'Información', { duration: 5000 });
          }

          if (this.previsualizacion) {
            // Enviar solo el payload (que es el RegistroServiceInDto) al método registerTest
            this.previsualizacion.registerTest(JSON.stringify(payloadData));
          } else {
             this.toastrService.danger('Error: Componente de previsualización no encontrado.', 'Error Fatal', { duration: 5000 });
             this.resultado = { registrosExitosos: 0, registrosFallidos: 0, registrosOmitidos: 0, respuesta: 'Error: Componente de previsualización no encontrado.' };
             this.errores = [];
             if (this.stepper) { this.stepper.selectedIndex = 2; }
          }
        } else {
          // Esto podría ocurrir si response.payload es null o response.payload.registro es undefined
          // o si la propia 'response' es null/undefined (ej. error HTTP no capturado por el .catch del servicio)
          console.error('La respuesta de obtenerOrders no tiene la estructura esperada (payload o payload.registro es nulo/undefined):', response);
          this.toastrService.danger('Error al procesar la respuesta de Shopify: estructura de payload inesperada.', 'Error', { duration: 5000 });
          this.resultado = { registrosExitosos: 0, registrosFallidos: 0, registrosOmitidos: 0, respuesta: 'Error: Respuesta de servidor (Shopify) con formato inesperado o sin datos de pedidos.' };
          this.errores = [];
          if (this.stepper) {
            this.stepper.selectedIndex = 2;
          }
        }
      },
      (error) => {
        console.error('Error en shopifyService.obtenerOrders:', error);
        const errorMessage = error.error?.responseMessage || error.message || 'Error desconocido al conectar con Shopify.';
        this.toastrService.danger(`Error al conectar con Shopify: ${errorMessage}`, 'Error', { duration: 5000 });
        this.resultado = { registrosExitosos: 0, registrosFallidos: 0, registrosOmitidos: 0, respuesta: `Error al conectar con Shopify: ${errorMessage}` };
        this.errores = [];
        if (this.stepper) {
            this.stepper.selectedIndex = 2;
        }
      }
    );
  }
}
