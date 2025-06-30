import { Component, OnInit } from '@angular/core';
import { Usuario } from 'src/app/models/usuario.model';

@Component({
  selector: 'app-gestion-usuarios',
  templateUrl: './gestion-usuarios.component.html',
  styleUrls: ['./gestion-usuarios.component.scss']
})
export class GestionUsuariosComponent implements OnInit {

  constructor() { }

  registros:  Usuario[];
  loading: boolean;

  setRegistros (registros: Usuario[]){
    this.registros = registros;
  }

  setLoading (loading: boolean) {
    this.loading = loading;
  }

  ngOnInit(): void {
  }

}
