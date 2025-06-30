import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Tienda } from 'src/app/models/tienda.model';
import { TiendaService } from 'src/app/services/tienda.service';

@Component({
  selector: 'app-filtro-consulta-tiendas',
  templateUrl: './filtro-consulta-tiendas.component.html',
  styleUrls: ['./filtro-consulta-tiendas.component.scss']
})

export class FiltroConsultaTiendasComponent implements OnInit {
  isAdmin: boolean;

  @Output() registros: any = new EventEmitter<Tienda[]>();
  @Output() loading: any = new EventEmitter<boolean>();

  constructor(private tiendaService: TiendaService) { }

  ngOnInit(): void {
  }

  obtenerTiendas() {

    this.loading.emit(true);

    this.tiendaService.obtenerTiendas().subscribe(
      (resp: Tienda[]) => {
        this.registros.emit(resp);
        this.loading.emit(false);
      }, (error) => {
        this.loading.emit(false);
        console.error(error);
      }
    );
  }
}