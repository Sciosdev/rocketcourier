import { Component, OnInit } from '@angular/core';
import { Tienda } from 'src/app/models/tienda.model';

@Component({
  selector: 'app-gestion-tiendas',
  templateUrl: './gestion-tiendas.component.html',
  styleUrls: ['./gestion-tiendas.component.scss']
})
export class GestionTiendasComponent implements OnInit {

  constructor() { }

  
  registros:  Tienda[];
  loading: boolean;

  setRegistros (registros: Tienda[]){
    this.registros = registros;
  }

  setLoading (loading: boolean) {
    this.loading = loading;
  }

  ngOnInit(): void {
  }

}
