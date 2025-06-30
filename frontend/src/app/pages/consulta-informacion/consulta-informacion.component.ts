import { Component, OnInit } from '@angular/core';
import { Estatus } from 'src/app/models/estatus.model';
import { RegistroTable } from 'src/app/models/registro.table.model';

@Component({
  selector: 'app-consulta-informacion',
  templateUrl: './consulta-informacion.component.html',
  styleUrls: ['./consulta-informacion.component.scss']
})
export class ConsultaInformacionComponent implements OnInit {

  constructor(){ }

  registros:  RegistroTable[];
  loading: boolean;
  vendor: String;
  estatus: Estatus;

  ngOnInit(): void {
  }
  setRegistros($event) {
    this.registros = $event;
  }
  setLoading($event) {
    this.loading = $event;
  }
  setVendor($event) {
    this.vendor = $event;
  }
  setEstatus($event) {
    this.estatus = $event;
  }
}
