import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-carga-layout',
  templateUrl: './carga-layout.component.html',
  styleUrls: ['./carga-layout.component.scss']
})
export class CargaLayoutComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
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
}
