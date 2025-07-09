import { Component, OnInit, ViewChild } from '@angular/core';
import { NbAuthOAuth2JWTToken, NbAuthService } from '@nebular/auth';
import { NbStepperComponent, NbToastrService } from '@nebular/theme';
import { PrevisualizacionComponent } from './previsualizacion/previsualizacion.component';
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


  archivoCargado:any;
  procesado:boolean = false; // Inicializar procesado
  tipoCarga: number;
  resultado: any;
  errores: any[] = []; // Inicializar errores

  constructor(
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
  }

}
