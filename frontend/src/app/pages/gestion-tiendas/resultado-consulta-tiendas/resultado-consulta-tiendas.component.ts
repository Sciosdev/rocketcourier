import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { Tienda } from 'src/app/models/tienda.model';
import { TiendaService } from 'src/app/services/tienda.service';
import { ModificacionTiendaComponent } from '../popups/modificacion-tienda/modificacion-tienda.component';
import { AltaTiendaComponent } from '../popups/alta-tienda/alta-tienda.component';
import { GlobalAcceptanceComponent } from '../../common-popups/global-acceptance/global-acceptance.component';

@Component({
  selector: 'app-resultado-consulta-tiendas',
  templateUrl: './resultado-consulta-tiendas.component.html',
  styleUrls: ['./resultado-consulta-tiendas.component.scss'],
})
export class ResultadoConsultaTiendasComponent
  implements OnInit, AfterViewInit
{
  @Input() registros: Tienda[];
  @Output() loading: any = new EventEmitter<boolean>();

  columns: any[] = [];
  defaultColumns = [
    'Nombre',
    'Correo',
    'Telefono',
    'Sitio',
    'Direccion',
    'Acciones',
  ];

  displayedColumns: string[];

  dataSource: MatTableDataSource<Tienda>;

  constructor(
    private dialogService: NbDialogService,
    private tiendaService: TiendaService,
    private toastrService: NbToastrService
  ) {}

  @ViewChild(MatPaginator) paginator: MatPaginator;

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource(this.registros);
    this.dataSource.paginator = this.paginator;
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }

    this.columns = [];
    this.columns.push(...this.defaultColumns);

    this.displayedColumns = this.columns;
  }

  ngOnChanges(): void {
    this.dataSource = new MatTableDataSource(this.registros);
    this.dataSource.paginator = this.paginator;
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }

    this.columns = [];
    this.columns.push(...this.defaultColumns);

    this.displayedColumns = this.columns;
  }

  modificarTienda(tienda: Tienda) {
    this.dialogService
      .open(ModificacionTiendaComponent, {
        closeOnBackdropClick: false,
        closeOnEsc: true,
        context: { tienda: tienda },
      })
      .onClose.subscribe((response) => {
        if (response && response.accept) {
          if (response.tienda.logo == 'nologo') response.tienda.logo = null;

          this.loading.emit(true);
          this.tiendaService.actualizarTienda(response.tienda).subscribe(
            (success) => {
              this.setTienda(response.tienda, tienda);

              if (response.tienda.logo) {
                if (response.tienda.logo == 'nologo') tienda.logo = null;
                else tienda.logo = response.tienda.logo;
              } else {
                tienda.logo = null;
              }

              this.toastrService.success(
                'La tienda fue actualizada correctamente',
                'Actualización'
              );
              this.loading.emit(false);
            },
            (error) => {
              this.toastrService.danger(
                'La tienda no fue actualizada correctamente',
                'Actualización'
              );
              this.loading.emit(false);
            }
          );
        }
      });
  }

  eliminarTienda(tienda: Tienda) {
    this.dialogService
      .open(GlobalAcceptanceComponent, {
        closeOnBackdropClick: false,
        closeOnEsc: true,
        context: {
          headerMessage: 'Eliminación de tienda',
          bodyMessage:
            '¿Desea eliminar la tienda: [' + tienda.nombreTienda + ']?',
          acceptanceLabel: 'Si',
          cancelLabel: 'No',
        },
      })
      .onClose.subscribe((response) => {
        console.log(response);
        if (response.accept == true) {
          this.loading.emit(true);
          this.tiendaService.eliminarTienda(tienda.id).subscribe(
            (response: any) => {
              if (response.response) {
                this.toastrService.success(
                  response.responseMessage,
                  'Eliminación'
                );
                this.registros = this.arrayRemove(this.registros, tienda);

                this.dataSource = new MatTableDataSource(this.registros);
                this.dataSource.paginator = this.paginator;
                if (this.dataSource.paginator) {
                  this.dataSource.paginator.firstPage();
                }
              } else {
                this.toastrService.danger(
                  response.responseMessage,
                  'Eliminación'
                );
              }

              this.loading.emit(false);
            },
            (error) => {
              this.toastrService.danger(
                'La tienda no se pudo eliminar',
                'Eliminación'
              );
              this.loading.emit(false);
            }
          );
        }
      });
  }

  setTienda(tiendaOrigen: Tienda, tiendaDestino: Tienda) {
    if (tiendaOrigen.id) tiendaDestino.id = tiendaOrigen.id;

    if (tiendaOrigen.nombreTienda)
      tiendaDestino.nombreTienda = tiendaOrigen.nombreTienda;

    if (tiendaOrigen.sitio) tiendaDestino.sitio = tiendaOrigen.sitio;

    if (tiendaOrigen.rutRazonSocial)
      tiendaDestino.rutRazonSocial = tiendaOrigen.rutRazonSocial;

    if (tiendaOrigen.giroComercial)
      tiendaDestino.giroComercial = tiendaOrigen.giroComercial;

    if (tiendaOrigen.direccion)
      tiendaDestino.direccion = tiendaOrigen.direccion;

    if (tiendaOrigen.tipoProducto)
      tiendaDestino.tipoProducto = tiendaOrigen.tipoProducto;

    if (tiendaOrigen.canalVenta)
      tiendaDestino.canalVenta = tiendaOrigen.canalVenta;

    if (tiendaOrigen.preferenciaPagoFactura)
      tiendaDestino.preferenciaPagoFactura =
        tiendaOrigen.preferenciaPagoFactura;

    if (tiendaOrigen.email) tiendaDestino.email = tiendaOrigen.email;

    if (tiendaOrigen.telefono) tiendaDestino.telefono = tiendaOrigen.telefono;

    if (tiendaOrigen.activo) tiendaDestino.activo = tiendaOrigen.activo;

    if (tiendaOrigen.logo) tiendaDestino.logo = tiendaOrigen.logo;
  }

  arrayRemove(arr: Tienda[], value: Tienda) {
    return arr.filter(function (ele) {
      return ele.id != value.id;
    });
  }

  crearTienda() {
    this.dialogService
      .open(AltaTiendaComponent, {
        closeOnBackdropClick: false,
        closeOnEsc: true,
      })
      .onClose.subscribe((response) => {
        if (response && response.accept) {
          this.loading.emit(true);
          this.tiendaService.crearTienda(response.tienda).subscribe(
            (ntienda:Tienda) => {

              let tienda = new Tienda();
              this.toastrService.success(
                'La tienda fue creada correctamente',
                'Crear'
              );
              this.loading.emit(false);

              tienda.setTienda(ntienda);

              this.registros.push(tienda);
              this.dataSource = new MatTableDataSource(this.registros);
              this.dataSource.paginator = this.paginator;
              if (this.dataSource.paginator) {
                this.dataSource.paginator.lastPage();
              }
            },
            (error) => {
              this.toastrService.success('La tienda no fue creada', 'Crear');
              this.loading.emit(false);
            }
          );
        }
      });
  }
}
