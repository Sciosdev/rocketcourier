import {
  Component,
  EventEmitter,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { NbAuthOAuth2JWTToken, NbAuthService } from '@nebular/auth';
import { NbAccessChecker } from '@nebular/security';
import { NbCalendarRange, NbDateService } from '@nebular/theme';
import { Estatus } from 'src/app/models/estatus.model';
import { RegistroTable } from 'src/app/models/registro.table.model';
import { EstatusService } from 'src/app/services/estatus.service';
import { RegistroService } from 'src/app/services/registro.service';
import { UsuarioService } from 'src/app/services/usuario.service';
import Swal from 'sweetalert2';
import { UsuarioCompleto } from '../../../models/usuario-completo.model';

@Component({
  selector: 'app-filtro',
  templateUrl: './filtro.component.html',
  styleUrls: ['./filtro.component.scss'],
})
export class FiltroComponent implements OnInit, OnChanges {
  @Output() registros: any = new EventEmitter<RegistroTable[]>();
  @Output() loading: any = new EventEmitter<boolean>();
  @Output() vendor: any = new EventEmitter<String>();
  @Output() sEstatus: any = new EventEmitter<Estatus>();
  @Output() courier: any = new EventEmitter<String>();

  users_combo: any[] = [];
  courier_combo: any[] = [];
  range: NbCalendarRange<Date>;
  checked: boolean = false;
  checked_courier: boolean = false;
  estatus: any[] = [];
  selectedEstatus: Estatus;

  selectFormControl = new FormControl('', Validators.required);
  selectCourierFormControl = new FormControl(
    { value: ''}
  );
  usuarioFormControl = new FormControl('', Validators.required);
  adminFormControl = new FormControl('');
  isAdmin: boolean;
  isCustomer: boolean;
  isCourier: boolean;
  access: boolean;
  selectedVendor;
  selectedCourier;

  vendorList = [];

  constructor(
    public accessChecker: NbAccessChecker,
    private authService: NbAuthService,
    private registroService: RegistroService,
    private usuarioService: UsuarioService,
    private estatusService: EstatusService,
    protected dateService: NbDateService<Date>
  ) {
    this.range = {
      start: this.monthStart,
      end: this.monthEnd,
    };
  }
  ngOnChanges(): void {
    this.authService.isAuthenticatedOrRefresh().subscribe((authenticated) => {
      if (authenticated) {
        this.authService.getToken().subscribe((token: NbAuthOAuth2JWTToken) => {
          if (token.isValid()) {
            let user = token.getAccessTokenPayload();
            this.loggedUser = user.user_name;
            this.usuarioFormControl.setValue(user.fullname);
            this.isAdmin = this.hasAccess('filtro', ['admin']);
            this.isCustomer = this.hasAccess('filtro', ['customer']);
            this.isCourier = this.hasAccess('filtro', ['courier']);
          }
        });
      }
    });
  }

  get monthStart(): Date {
    return this.dateService.today();
  }

  get monthEnd(): Date {
    return this.dateService.addDay(new Date(), 1);
  }

  ngOnInit(): void {
    this.usuarioFormControl.disable();

    this.usuarioService.obtenerVendedores().subscribe((resp: any[]) => {
      resp.forEach((element) => {
        this.users_combo.push(element);
      });
    });
    this.usuarioService.obtenerCouriers().subscribe((resp: any[]) => {
      resp.forEach((element) => {
        this.courier_combo.push(element);
      });
    });

    this.authService.isAuthenticatedOrRefresh().subscribe((authenticated) => {
      if (authenticated) {
        this.authService.getToken().subscribe((token: NbAuthOAuth2JWTToken) => {
          if (token.isValid()) {
            let user = token.getAccessTokenPayload();
            this.loggedUser = user.user_name;
            this.usuarioFormControl.setValue(user.fullname);
            this.isAdmin = this.hasAccess('filtro', ['admin']);
            this.isCustomer = this.hasAccess('filtro', ['customer']);
            this.isCourier = this.hasAccess('filtro', ['courier']);

            this.estatusService
              .obtenerEstatus(this.loggedUser)
              .subscribe((resp: Estatus[]) => {
                this.estatus = resp;
              });
          }
        });
      }
    });
  }

  loggedUser: any;

  hasAccess(permission, resources: any[]) {
    let access = false;
    resources.forEach((element) => {
      this.accessChecker.isGranted(permission, element).subscribe((granted) => {
        if (granted) access = true;
      });
    });

    return access;
  }

  validateInput() {
    if (!this.isAdmin) {
      return (
        this.usuarioFormControl.hasError('required') ||
        this.selectFormControl.hasError('required')
      );
    } else {
        return (
          this.adminFormControl.hasError('required') ||
          this.selectFormControl.hasError('required')
        );
    }
  }

  obtenerRegistros() {
    this.loading.emit(true);

    if (this.isCustomer) {
      this.selectedVendor = this.loggedUser;
      this.selectedCourier = null;
    }

    this.vendor.emit(this.selectedVendor);

    if (this.isCourier) {
      this.selectedCourier = this.loggedUser;
      this.selectedVendor = null;
    }

    this.courier.emit(this.selectedCourier);

    if (this.checked) {
      if (this.range.start == undefined || this.range.end == undefined) {
        this.loading.emit(false);
        Swal.fire(
          'Error',
          'Por favor asegurese que el rango de fechas es correcto',
          'error'
        );
      } else {
        this.registroService
          .obtenerRegistrosPorFecha(
            this.selectedVendor,
            this.range.start,
            this.range.end,
            this.selectedEstatus.id,
            this.selectedCourier
          )
          .subscribe(
            (response: RegistroTable[]) => {
              this.sEstatus.emit(this.selectedEstatus);
              this.registros.emit(response);
              this.loading.emit(false);
            },
            (error) => {
              console.warn(error);
              Swal.fire(
                'Precaución',
                'Ocurrió un error inesperado con la conexión, por favor intente nuevamente.',
                'warning'
              );
              this.loading.emit(false);
            }
          );
      }
    } else {
      this.registroService
        .obtenerRegistros(
          this.selectedVendor,
          this.selectedEstatus.id,
          this.selectedCourier
        )
        .subscribe(
          (response: RegistroTable[]) => {
            this.sEstatus.emit(this.selectedEstatus);
            this.registros.emit(response);
            this.loading.emit(false);
          },
          (error) => {
            console.warn(error);
            Swal.fire(
              'Precaución',
              'Ocurrió un error inesperado con la conexión, por favor intente nuevamente.',
              'warning'
            );
            this.loading.emit(false);
          }
        );
    }

    this.selectCourierFormControl.reset();
    this.selectCourierFormControl.setValue(this.selectedCourier);
    this.checked_courier = false;
  }

  setVendor(event: UsuarioCompleto) {
    console.log(this.vendorList);
    if (this.vendorList.includes(event.name)) {
      this.vendorList = [...this.arrayDelete(this.vendorList, event.name)];
    } else this.vendorList.push(event.name);

    console.log(this.vendorList);

  }

  arrayDelete(list: string[], value: string) {
    return list.filter((val) => {
      return val != value;
    });
  }
}
