import { SelectionModel } from '@angular/cdk/collections';
import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { NbAuthOAuth2JWTToken, NbAuthService } from '@nebular/auth';
import { NbAccessChecker } from '@nebular/security';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { PrimeNGConfig } from 'primeng/api';

import { Estatus } from 'src/app/models/estatus.model';
import { RegistroTable } from 'src/app/models/registro.table.model';
import { ScheduleServiceInDto } from 'src/app/models/ScheduleServiceInDto.model';
import { RegistroService } from 'src/app/services/registro.service';
import { AcceptanceComponent } from '../popups/acceptance/acceptance.component';
import { ScheduleComponent } from '../popups/schedule/schedule.component';

import { saveAs } from 'file-saver';
import { CambioEstatusComponent } from '../popups/cambio-estatus/cambio-estatus.component';
import { EstatusService } from 'src/app/services/estatus.service';
import { CambioEstatusMultipleComponent } from '../popups/cambio-estatus-multiple/cambio-estatus-multiple.component';
import { GlobalAcceptanceComponent } from '../../common-popups/global-acceptance/global-acceptance.component';
import { TipoEstatus } from '../../../models/tipo.estatus.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-resultado-consulta',
  templateUrl: './resultado-consulta.component.html',
  styleUrls: ['./resultado-consulta.component.scss'],
})
export class ResultadoConsultaComponent
  implements OnInit, OnChanges, AfterViewInit
{
  @Input() registros: RegistroTable[];
  @Input() vendor;
  @Input() sEstatus: Estatus;

  @Output() regis: any = new EventEmitter<RegistroTable[]>();
  @Output() loading: any = new EventEmitter<boolean>();

  filterSelectObj = [];
  filterValues = {};

  columns: any[] = [];
  defaultColumns = [
    'OrderKey',
    'Name',
    'Email',
    'Shipping City',
    'Shipping Address 1',
    'Shipping Address 2',
    'Status',
    'CargaDT',
    'Scheduled',
    'Comentario',
    'Courier',
    'Vendedor',
  ];

  displayedColumns: string[];
  dataSource: MatTableDataSource<RegistroTable>;

  initialSelection: RegistroTable[] = [];
  allowMultiSelect = true;
  selection: SelectionModel<RegistroTable>;
  ToBeScheduled: RegistroTable[] = [];

  loggedUser;

  isCustomer: boolean;
  isAdmin: boolean;
  isCourier: boolean;

  authorizedStatus: any[] = [];

  scheduleAccepted: RegistroTable[] = [];
  scheduleRejected: RegistroTable[] = [];
  scheduleModified: RegistroTable[] = [];
  statusChange: RegistroTable[] = [];

  comunas: any[] = [];

  selectedComuna: string = '';

  filtro;

  constructor(
    public accessChecker: NbAccessChecker,
    private dialogService: NbDialogService,
    private registroService: RegistroService,
    private authService: NbAuthService,
    private estatusService: EstatusService,
    protected cd: ChangeDetectorRef,
    private _snackBar: MatSnackBar,
    private primeNGConfig: PrimeNGConfig,
    private toastrService: NbToastrService
  ) {
    this.selection = new SelectionModel<any>(
      this.allowMultiSelect,
      this.initialSelection
    );

    this.filterSelectObj = [
      {
        name: 'Comuna',
        columnProp: 'shippingCity',
        options: [],
      },
    ];
  }

  @ViewChild(MatPaginator) paginator: MatPaginator;

  getFilterObject(fullObj, key) {
    const uniqChk = [];
    fullObj.filter((obj) => {

      if (!uniqChk.includes(this.capitalize(obj[key]))) {
        uniqChk.push(this.capitalize(obj[key]));
      }

      return obj;
    });

    uniqChk.sort();
    return uniqChk;
  }

  capitalize(mySentence) {
    return mySentence
      .toLowerCase()
      .trim()
      .replace(/(^\w{1})|(\s+\w{1})/g, (letter) => letter.toUpperCase());
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  ngOnInit(): void {
    this.loadAccess();
    this.loadUser();
    this.authorizedStatus = [];
    this.estatusService.obtenerEstatusChange(this.loggedUser).subscribe(
      (response: Estatus[]) => {
        response.forEach((r) => {
          this.authorizedStatus.push(r.id);
        });

        this.columns = [];
        if (
          this.canRenderCustomer() ||
          this.canRenderAdmin() ||
          this.canRenderCambioEstatus() ||
          this.canRenderDiscarded() ||
          this.canRenderEtiqueta()
        ) {
          this.columns.push('select', ...this.defaultColumns);
        } else this.columns.push(...this.defaultColumns);

        if (this.canRenderEtiqueta()) {
          this.columns.push('actions');
        }

        if (this.canRenderDeliveryComment()) {
          this.columns.push('DeliveryComment');
        }

        if (
          this.canRenderCambioEstatus() &&
          !this.columns.includes('CambioEstatus')
        ) {
          this.columns.push('CambioEstatus');
        }

        this.displayedColumns = this.columns;
      },
      (error) => {
        console.error(error);
        this.toastrService.danger(
          'Ocurrió un error al obtener el estatus',
          'Estatus Change'
        );
      }
    );

    this.registroService.obtenerComunas().subscribe(
      (response: any[]) => {
        this.comunas = response;
      },
      (error) => {
        console.error(error);
      }
    );

    this.primeNGConfig.setTranslation({
      dayNames: [
        'domingo',
        'lunes',
        'martes',
        'miércoles',
        'jueves',
        'viernes',
        'sábado',
      ],
      dayNamesShort: ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb'],
      dayNamesMin: ['D', 'L', 'M', 'X', 'J', 'V', 'S'],
      monthNames: [
        'Enero',
        'Febrero',
        'Marzo',
        'Abril',
        'Mayo',
        'Junio',
        'Julio',
        'Agosto',
        'Septiembre',
        'Octubre',
        'Noviembre',
        'Diciembre',
      ],
      monthNamesShort: [
        'ene',
        'feb',
        'mar',
        'abr',
        'may',
        'jun',
        'jul',
        'ago',
        'sep',
        'oct',
        'nov',
        'dic',
      ],
      today: 'Hoy',
      clear: 'Limpiar',
    });

    this.dataSource.filterPredicate = this.createFilter();

    this.filterSelectObj.filter((o) => {
      o.options = this.getFilterObject(this.registros, o.columnProp);
    });

    this.filterSelectObj.sort();
  }

  // Called on Filter change
  filterChange(filter, event) {
    //let filterValues = {}
    this.filterValues[filter.columnProp] = event.target.value
      .trim()
      .toLowerCase();

    this.dataSource.filter = JSON.stringify(this.filterValues);

    this.dataSource.paginator = this.paginator;
      this.selection.clear();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  // Custom filter method fot Angular Material Datatable
  createFilter() {
    let filterFunction = function (data: any, filter: string): boolean {
      let searchTerms = JSON.parse(filter);
      let isFilterSet = false;
      for (const col in searchTerms) {
        if (searchTerms[col].toString() !== '') {
          isFilterSet = true;
        } else {
          delete searchTerms[col];
        }
      }

      let nameSearch = () => {
        let found = false;
        if (isFilterSet) {
          for (const col in searchTerms) {
            if (
              data[col]
                .toString()
                .trim()
                .toLowerCase()
                .indexOf(searchTerms[col].trim().toLowerCase()) != -1 &&
              isFilterSet
            ) {
              found = true;
            }
          }
          return found;
        } else {
          return true;
        }
      };
      return nameSearch();
    };
    return filterFunction;
  }

  // Reset table filters
  resetFilters() {
    this.filterValues = {};
    this.filterSelectObj.forEach((value, key) => {
      value.modelValue = undefined;
    });
    this.dataSource.filter = '';
  }

  applyFilter(filtro) {
    this.filtro = filtro;
    console.log(filtro);

    if (filtro) {
      this.dataSource.filter = filtro.trim().toLowerCase();

      if (this.dataSource.paginator) {
        this.dataSource.paginator.firstPage();
      }
    } else {
      this.dataSource = new MatTableDataSource(this.registros);
      this.dataSource.paginator = this.paginator;
      this.selection.clear();
    }
  }

  canRenderCambioEstatus() {
    return this.authorizedStatus.includes(this.sEstatus.id);
  }

  ngOnChanges(): void {
    this.dataSource = new MatTableDataSource(this.registros);
    this.dataSource.paginator = this.paginator;
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
    this.selection.clear();
    this.ToBeScheduled = [];
    this.columns = [];
    if (
      this.canRenderCustomer() ||
      this.canRenderAdmin() ||
      this.canRenderCambioEstatus() ||
      this.canRenderDiscarded() ||
      this.canRenderEtiqueta()
    ) {
      this.columns.push('select', ...this.defaultColumns);
    } else this.columns.push(...this.defaultColumns);

    if (this.canRenderEtiqueta()) {
      this.columns.push('actions');
    }

    if (this.canRenderDeliveryComment()) {
      this.columns.push('DeliveryComment');
    }

    if (
      this.canRenderCambioEstatus() &&
      !this.columns.includes('CambioEstatus')
    ) {
      this.columns.push('CambioEstatus');
    }

    this.selectedComuna = '';

    this.displayedColumns = this.columns;

    this.resetFilters();

    this.dataSource.filterPredicate = this.createFilter();

    this.filterSelectObj.filter((o) => {
      o.options = this.getFilterObject(this.registros, o.columnProp);
    });

    this.filterSelectObj.sort();
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.filteredData.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected()
      ? this.selection.clear()
      : this.dataSource.filteredData.forEach((row) => this.selection.select(row));
  }

  solicitarAgenda() {
    this.dialogService
      .open(ScheduleComponent, {
        closeOnBackdropClick: false,
        closeOnEsc: true,
      })
      .onClose.subscribe((result: any) => {
        if (result != undefined && result.fecha != null) {
          this.selection.selected.forEach((element) => {
            element.scheduledDt = result.fecha;
            element.comment = result.comentario;
            if (!this.ToBeScheduled.includes(element)) {
              this.ToBeScheduled.push(element);
            }
          });
        }
        this.selection.clear();
      });
  }

  arrayRemove(arr: RegistroTable[], value: RegistroTable) {
    console.log('value: ' + value.orderkey);
    return arr.filter(function (ele) {
      return ele.orderkey != value.orderkey;
    });
  }

  cambiarEstatus(registroTable: RegistroTable) {
    this.dialogService
      .open(CambioEstatusComponent, {
        closeOnBackdropClick: false,
        context: { currentEstatus: this.sEstatus, registro: registroTable },
      })
      .onClose.subscribe((result: any) => {
        if (result) {
          this.loading.emit(true);
          this.estatusService
            .actualizarEstatus(
              result.estatus,
              registroTable.orderkey,
              this.loggedUser,
              result.courier
            )
            .subscribe(
              (success) => {
                this.registros = this.arrayRemove(
                  this.registros,
                  registroTable
                );
                this.dataSource = new MatTableDataSource(this.registros);
                this.dataSource.paginator = this.paginator;
                this.loading.emit(false);
                this.toastrService.success(
                  'Se actualizó correctamente el estatus',
                  'Cambio de estatus'
                );
              },
              (error) => {
                console.error(error);
                this.loading.emit(false);
                this.toastrService.danger(
                  'Ocurrió un error al cambiar el estatus',
                  'Cambio de estatus'
                );
              }
            );
        }
      });
  }

  cambiarEstatusMultiple() {
    this.dialogService
      .open(CambioEstatusMultipleComponent, {
        closeOnBackdropClick: false,
        context: {
          currentEstatus: this.sEstatus,
          totalRegistros: this.selection.selected.length,
        },
      })
      .onClose.subscribe((result: any) => {
        let orderKeys: string[] = [];
        this.selection.selected.forEach((register) => {
          orderKeys.push(register.orderkey);
        });

        if (result) {
          this.loading.emit(true);
          this.estatusService
            .actualizarListaEstatus(
              result.estatus,
              orderKeys,
              this.loggedUser,
              result.courier,
              result.comment
            )
            .subscribe(
              (success: any[]) => {
                console.warn(success);

                success.forEach((registro) => {
                  this.registros = this.arrayRemove(this.registros, registro);
                });

                this.selection.clear();
                this.statusChange = [];
                this.dataSource = new MatTableDataSource(this.registros);
                this.dataSource.paginator = this.paginator;
                this.loading.emit(false);
                this.toastrService.success(
                  'Se actualizó correctamente el estatus',
                  'Cambio de estatus'
                );
              },
              (error) => {
                console.error(error);
                this.loading.emit(false);
                this.toastrService.danger(
                  'Ocurrió un error al cambiar el estatus',
                  'Cambio de estatus'
                );
              }
            );
        }
      });
  }

  aceptarAgenda() {
    this.dialogService
      .open(AcceptanceComponent, {
        closeOnBackdropClick: false,
      })
      .onClose.subscribe((result: any) => {
        if (result.accepted) {
          this.loading.emit(true);
          this.scheduleAccepted.push(...this.selection.selected);

          this.selection.selected.forEach((ele) => {
            this.registros = this.arrayRemove(this.registros, ele);
          });

          let data: ScheduleServiceInDto[] = [];

          this.selection.selected.forEach((registro) => {
            let scheduleServiceInDto: ScheduleServiceInDto = {};
            scheduleServiceInDto.orderkey = registro.orderkey;
            try {
              scheduleServiceInDto.scheduledDate =
                registro.scheduledDt.toLocaleDateString() +
                ' ' +
                registro.scheduledDt.toLocaleTimeString();
            } catch (error) {
              let scheduleDate = new Date(
                Date.parse(registro.scheduledDt.toString())
              );
              scheduleServiceInDto.scheduledDate =
                scheduleDate.toLocaleDateString() +
                ' ' +
                scheduleDate.toLocaleTimeString();
            }
            scheduleServiceInDto.comment = registro.comment;
            scheduleServiceInDto.vendor = null;
            scheduleServiceInDto.user = this.loggedUser;
            scheduleServiceInDto.courier = result.courier;
            data.push(scheduleServiceInDto);
          });

          this.registroService.aceptarAgenda(data).subscribe(
            (response) => {
              this.loading.emit(false);
              this.toastrService.success(
                'Se acepto la agenda correctamente',
                'Proceso'
              );
            },
            (error) => {
              this.loading.emit(false);
              this.toastrService.danger(
                'Ocurrió un error al aceptar la agenda',
                'Proceso'
              );
            }
          );

          this.dataSource = new MatTableDataSource(this.registros);
          this.dataSource.paginator = this.paginator;
        }

        this.selection.clear();
      });
  }

  rechazarAgenda() {
    let data: ScheduleServiceInDto[] = [];

    this.dialogService
      .open(ScheduleComponent, {
        closeOnBackdropClick: false,
        context: { disabled: true },
      })
      .onClose.subscribe((result: any) => {
        if (result != null || result != undefined) {
          this.loading.emit(true);

          this.scheduleRejected.push(...this.selection.selected);

          this.selection.selected.forEach((ele) => {
            this.registros = this.arrayRemove(this.registros, ele);
          });

          this.selection.selected.forEach((registro) => {
            let scheduleServiceInDto: ScheduleServiceInDto = {};

            if (result != undefined && result.fecha != null) {
              scheduleServiceInDto.orderkey = registro.orderkey;
              try {
                scheduleServiceInDto.scheduledDate =
                  registro.scheduledDt.toLocaleDateString() +
                  ' ' +
                  registro.scheduledDt.toLocaleTimeString();
              } catch (error) {
                let scheduleDate = new Date(
                  Date.parse(registro.scheduledDt.toString())
                );
                scheduleServiceInDto.scheduledDate =
                  scheduleDate.toLocaleDateString() +
                  ' ' +
                  scheduleDate.toLocaleTimeString();
              }
              scheduleServiceInDto.comment = result.comentario;
              scheduleServiceInDto.vendor = this.vendor;
              scheduleServiceInDto.user = this.loggedUser;
              data.push(scheduleServiceInDto);
            }
          });

          this.registroService.rechazarAgenda(data).subscribe(
            (response) => {
              this.loading.emit(false);
              this.toastrService.success(
                'Se rechazo la agenda correctamente',
                'Proceso'
              );
              this.dataSource = new MatTableDataSource(this.registros);
              this.dataSource.paginator = this.paginator;
            },
            (error) => {
              this.loading.emit(false);
              this.toastrService.danger(
                'Ocurrió un error al rechazar la agenda',
                'Proceso'
              );
            }
          );
        }

        this.selection.clear();
      });
  }

  limpiarAgenda() {
    this.ToBeScheduled.forEach((element) => {
      element.scheduledDt = null;
      element.comment = null;
    });

    this.ToBeScheduled = [];
    this.selection.clear();
  }

  procesarAgenda() {
    this.loading.emit(true);
    let agenda: ScheduleServiceInDto[] = [];

    this.ToBeScheduled.forEach((registro) => {
      let scheduleServiceInDto: ScheduleServiceInDto = {};
      scheduleServiceInDto.orderkey = registro.orderkey;
      scheduleServiceInDto.scheduledDate =
        registro.scheduledDt.toLocaleDateString() +
        ' ' +
        registro.scheduledDt.toLocaleTimeString();
      scheduleServiceInDto.comment = registro.comment;
      scheduleServiceInDto.courier = registro.courier;
      scheduleServiceInDto.vendor = this.vendor;
      scheduleServiceInDto.user = this.loggedUser;
      agenda.push(scheduleServiceInDto);
    });

    this.registroService.solicitarAgenda(agenda).subscribe(
      (response) => {
        this.limpiarAgenda();
        this.registros = null;
        this.regis.emit(this.registros);
        this.loading.emit(false);
        this.toastrService.success(
          'Registros actualizados correctamente',
          'Proceso'
        );
      },
      (error) => {
        this.limpiarAgenda();
        this.registros = null;
        this.regis.emit(this.registros);
        this.loading.emit(false);
        this.toastrService.danger(
          'Ocurrió un error al actualizar los registros',
          'Proceso'
        );
      }
    );
  }

  imprimir(registro: RegistroTable) {
    this.loading.emit(true);
    this.registroService.obtenerEtiqueta(registro.orderkey).subscribe(
      (response: any) => {
        this.loading.emit(false);
        var blob = new Blob([response], { type: 'application/pdf' });
        saveAs(blob, registro.orderkey + '.pdf');
      },
      (error) => {
        this.loading.emit(false);
        console.error(error);
      }
    );
  }

  generarDescargaZip() {
    this.loading.emit(true);

    let orderkeys = [];

    this.selection.selected.forEach((registro) => {
      orderkeys.push(registro.orderkey);
    });

    this.registroService.obtenerEtiquetaZip(orderkeys).subscribe(
      (response: any) => {
        this.loading.emit(false);
        var blob = new Blob([response], { type: 'application/zip' });
        saveAs(blob, `Etiquetas_${this.getDateString()}.zip`);
      },
      (error) => {
        this.loading.emit(false);
        console.error(error);
      }
    );

    this.selection.clear();
  }

  getDateString() {
    const date = new Date();
    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    const time = date.getTime();
    return `${year}${month}${day}_${time}`;
  }

  descartarRegistro() {
    this.dialogService
      .open(GlobalAcceptanceComponent, {
        closeOnBackdropClick: false,
        closeOnEsc: true,
        context: {
          headerMessage: 'Descartar Registros',
          bodyMessage:
            '¿Desea descartar [' +
            this.selection.selected.length +
            '] registro(s)?',
          acceptanceLabel: 'Si',
          cancelLabel: 'No',
        },
      })
      .onClose.subscribe((response) => {
        if (response.accept == true) {
          let orderKeys: string[] = [];
          this.selection.selected.forEach((register) => {
            orderKeys.push(register.orderkey);
          });

          this.loading.emit(true);
          this.estatusService
            .descartarListaEstatus(
              TipoEstatus.DESCARTADO,
              orderKeys,
              this.loggedUser
            )
            .subscribe(
              (success: any[]) => {
                console.warn(success);

                success.forEach((registro) => {
                  this.registros = this.arrayRemove(this.registros, registro);
                });

                this.selection.clear();
                this.statusChange = [];
                this.dataSource = new MatTableDataSource(this.registros);
                this.dataSource.paginator = this.paginator;
                this.loading.emit(false);
                this.toastrService.success(
                  'Se descartó correctamente la selección',
                  'Descartar estatus'
                );
              },
              (error) => {
                console.error(error);
                this.loading.emit(false);
                this.toastrService.danger(
                  'Ocurrió un error al descartar la selección',
                  'Descartar estatus'
                );
              }
            );
        } else {
          this.selection.clear();
        }
      });
  }

  eliminarPedidos() {
    this.dialogService
      .open(GlobalAcceptanceComponent, {
        closeOnBackdropClick: false,
        closeOnEsc: true,
        context: {
          headerMessage: 'Eliminación de pedidos',
          bodyMessage:
            '¿Desea eliminar [' +
            this.selection.selected.length +
            '] pedido(s)?',
          acceptanceLabel: 'Si',
          cancelLabel: 'No',
        },
      })
      .onClose.subscribe((response) => {
        if (response.accept == true) {
          const deletes = this.selection.selected.map((register) =>
            this.registroService.eliminarRegistro(register.orderkey)
          );
          this.loading.emit(true);
          forkJoin(deletes).subscribe(
            () => {
              this.selection.selected.forEach((registro) => {
                this.registros = this.arrayRemove(this.registros, registro);
              });
              this.selection.clear();
              this.dataSource = new MatTableDataSource(this.registros);
              this.dataSource.paginator = this.paginator;
              this.loading.emit(false);
              this.toastrService.success(
                'Se eliminaron correctamente los pedidos',
                'Eliminación',
                { duration: 8000 }
              );
            },
            (error) => {
              console.error(error);
              this.loading.emit(false);
              this.toastrService.danger(
                'Ocurrió un error al eliminar los pedidos',
                'Eliminación',
                { duration: 8000 }
              );
            }
          );
        } else {
          this.selection.clear();
        }
      });
  }

  hasAccess(permission, resources: any[]) {
    let access = false;
    this.authService.isAuthenticatedOrRefresh().subscribe((authenticated) => {
      if (authenticated) {
        resources.forEach((element) => {
          this.accessChecker
            .isGranted(permission, element)
            .subscribe((granted) => {
              if (granted) access = true;
            });
        });
      } else {
        access = false;
      }
    });
    return access;
  }

  loadAccess() {
    this.isCustomer = this.hasAccess('filtro', ['customer']);
    this.isAdmin = this.hasAccess('filtro', ['admin']);
    this.isCourier = this.hasAccess('filtro', ['courier']);
  }

  canRenderCustomer() {
    if (this.isCustomer) {
      return this.sEstatus.tipo === 'inicial';
    } else {
      return false;
    }
  }

  canRenderAdmin() {
    if (this.isAdmin) {
      return this.sEstatus.id == 2;
    } else {
      return false;
    }
  }

  canRenderDiscarded() {
    if (this.isAdmin) {
      return this.sEstatus.id != TipoEstatus.DESCARTADO;
    } else {
      return false;
    }
  }

  canRenderEtiqueta() {
    return this.sEstatus.id == 3;
  }

  canRenderDeliveryComment() {
    return this.sEstatus.id == 10 || this.sEstatus.id == 11;
  }

  loadUser() {
    this.authService.isAuthenticatedOrRefresh().subscribe((authenticated) => {
      if (authenticated) {
        this.authService.getToken().subscribe((token: NbAuthOAuth2JWTToken) => {
          if (token.isValid()) {
            let user = token.getAccessTokenPayload();
            this.loggedUser = user.user_name;
          }
        });
      }
    });
  }
}
