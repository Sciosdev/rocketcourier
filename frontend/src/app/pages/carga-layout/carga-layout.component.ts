import { Component, OnInit, ViewChild } from '@angular/core';
import { NbAuthOAuth2JWTToken, NbAuthService } from '@nebular/auth';
import { NbStepperComponent } from '@nebular/theme';
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

  @ViewChild('stepper')
  stepper: NbStepperComponent;

  user: any;

  fechaInicio: string;
  fechaFin: string;

import { Component, OnInit, ViewChild } from '@angular/core';
import { NbAuthOAuth2JWTToken, NbAuthService } from '@nebular/auth';
import { NbStepperComponent, NbToastrService } from '@nebular/theme'; // Asegúrate de importar NbToastrService
import { PrevisualizacionComponent } from './previsualizacion/previsualizacion.component';
import { ShopifyService } from '../../services/shopify.service';
import { RegistroService } from '../../services/registro.service'; // Importar RegistroService

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

  constructor(
    private shopifyService: ShopifyService,
    private authService: NbAuthService,
    private registroService: RegistroService, // Inyectar RegistroService
    private toastrService: NbToastrService // Inyectar NbToastrService
  ) { }

  ngOnInit(): void {
    this.authService.onTokenChange().subscribe((token: NbAuthOAuth2JWTToken) => {
      if (token.isValid()) {
        this.user = token.getAccessTokenPayload();
      }
    });

    const today = new Date();
    const year = today.getFullYear();
    const month = ('0' + (today.getMonth() + 1)).slice(-2); // Agrega 0 si es necesario y toma los últimos 2 dígitos
    const day = ('0' + today.getDate()).slice(-2); // Agrega 0 si es necesario y toma los últimos 2 dígitos

    this.fechaInicio = `${year}-${month}-${day}`;
    this.fechaFin = `${year}-${month}-${day}`;
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
      this.toastrService.show('No se pudo obtener la información del usuario.', 'Error', { status: 'danger' });
      return;
    }
    if (!this.fechaInicio || !this.fechaFin) {
      this.toastrService.show('Por favor, seleccione fecha de inicio y fin.', 'Advertencia', { status: 'warning' });
      return;
    }

    const vendor = this.user.user_name || this.user; // Asegúrate que 'this.user' tenga 'user_name'

    const startDate = new Date(this.fechaInicio);
    startDate.setUTCHours(0, 0, 0, 0); // Inicio del día en UTC
    const inicioISO = startDate.toISOString();

    const endDate = new Date(this.fechaFin);
    endDate.setUTCHours(23, 59, 59, 999); // Final del día en UTC
    const finISO = endDate.toISOString();

    console.log(`Solicitando pedidos para vendor: ${vendor}, inicio: ${inicioISO}, fin: ${finISO}`);

    // Move the wizard to the preview step before sending the request
    // Esto se manejará después de recibir la respuesta de obtenerOrders

    this.shopifyService.obtenerOrders(vendor, inicioISO, finISO).subscribe(
      (data: any) => {
        console.log('Respuesta de shopifyService.obtenerOrders:', JSON.stringify(data));

        // Mover al paso de previsualización/resultados
        if (this.stepper) {
            this.stepper.selectedIndex = 1; // Índice del paso de previsualización/resultados
        }

        if (data && data.registro) {
          if (data.registro.length > 0) {
            this.toastrService.info(`Se encontraron ${data.registro.length} pedidos de Shopify. Enviando para registro...`, 'Información');
            // Enviar los datos al previsualizacionComponent para que llame a registrarCarga
             if (this.previsualizacion) {
 компенсироватьthis.previsualizacion.registerTest(JSON.stringify(data));
            } else {
                 this.toastrService.danger('Error: Componente de previsualización no encontrado.', 'Error');
                 this.resultado = { registrosExitosos: 0, registrosFallidos: 0, registrosOmitidos: 0, respuesta: 'Error: Componente de previsualización no encontrado.' };
                 this.errores = [];
                 if (this.stepper) { this.stepper.selectedIndex = 2; } // Ir a resultados
            }
          } else {
            this.toastrService.warning('No se encontraron pedidos de Shopify para las fechas seleccionadas.', 'Información');
            // Si no hay pedidos, igualmente se puede llamar a registerTest con datos vacíos para que muestre 0 resultados.
            if (this.previsualizacion) {
                this.previsualizacion.registerTest(JSON.stringify(data)); // data ya tiene registro: []
            } else {
                this.toastrService.danger('Error: Componente de previsualización no encontrado.', 'Error');
                this.resultado = { registrosExitosos: 0, registrosFallidos: 0, registrosOmitidos: 0, respuesta: 'No se encontraron pedidos.' };
                this.errores = [];
                if (this.stepper) { this.stepper.selectedIndex = 2; } // Ir a resultados
            }
          }
        } else {
          console.error('La respuesta de obtenerOrders no tiene la estructura esperada:', data);
          this.toastrService.danger('Error al procesar la respuesta de Shopify. Estructura inesperada.', 'Error');
          this.resultado = { registrosExitosos: 0, registrosFallidos: 0, registrosOmitidos: 0, respuesta: 'Error: Respuesta inesperada del servidor al obtener pedidos.' };
          this.errores = [];
          if (this.stepper) {
            this.stepper.selectedIndex = 2; // Ir a la pestaña de resultados
          }
        }
      },
      (error) => {
        console.error('Error en shopifyService.obtenerOrders:', error);
        this.toastrService.danger('Error al conectar con Shopify: ' + (error.error?.responseMessage || error.message || 'Error desconocido'), 'Error');
        this.resultado = { registrosExitosos: 0, registrosFallidos: 0, registrosOmitidos: 0, respuesta: 'Error al conectar con Shopify.' };
        this.errores = [];
        if (this.stepper) {
            this.stepper.selectedIndex = 2; // Ir a la pestaña de resultados en caso de error
        }
      }
    );
  }
}
