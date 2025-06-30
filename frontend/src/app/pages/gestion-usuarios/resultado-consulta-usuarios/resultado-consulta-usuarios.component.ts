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
import { UsuarioCompleto } from 'src/app/models/usuario-completo.model';
import { Usuario } from 'src/app/models/usuario.model';
import { UsuarioService } from 'src/app/services/usuario.service';
import { GlobalAcceptanceComponent } from '../../common-popups/global-acceptance/global-acceptance.component';
import { ModificarUsuarioComponent } from '../popups/modificar-usuario/modificar-usuario.component';
import { AltaUsuarioComponent } from '../popups/alta-usuario/alta-usuario.component';

@Component({
  selector: 'app-resultado-consulta-usuarios',
  templateUrl: './resultado-consulta-usuarios.component.html',
  styleUrls: ['./resultado-consulta-usuarios.component.scss'],
})
export class ResultadoConsultaUsuariosComponent
  implements OnInit, AfterViewInit
{
  @Input() registros: Usuario[];
  @Output() loading: any = new EventEmitter<boolean>();

  columns: any[] = [];
  defaultColumns = [
    'Nombre',
    'Rol',
    'Tienda',
    'Correo',
    'Telefono',
    'Acciones',
  ];

  displayedColumns: string[];

  dataSource: MatTableDataSource<Usuario>;

  constructor(
    private dialogService: NbDialogService,
    private usuarioService: UsuarioService,
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

  crearUsuario() {
    this.dialogService
      .open(AltaUsuarioComponent, {
        closeOnBackdropClick: false,
        closeOnEsc: true,
        hasScroll: true,
      })
      .onClose.subscribe((response) => {
        console.log(response);
        if (response && response.accept) {
          this.loading.emit(true);
          this.usuarioService.agregarUsuario(response.usuario).subscribe(
            (nusuario: UsuarioCompleto) => {
              let usuario = new UsuarioCompleto();
              this.toastrService.success(
                'El usuario fue creado correctamente',
                'Crear'
              );
              this.loading.emit(false);

              usuario.setUsuario(nusuario);

              let usuarioSimple: Usuario = this.mapUsuarioCompletoToUsuario(
                usuario,
                response.tienda
              );

              this.registros.push(usuarioSimple);
              this.dataSource = new MatTableDataSource(this.registros);
              this.dataSource.paginator = this.paginator;
              if (this.dataSource.paginator) {
                this.dataSource.paginator.lastPage();
              }
            },
            (error) => {
              this.toastrService.warning(error.error, 'Error al crear');
              this.loading.emit(false);
            }
          );
        }
      });
  }

  modificarUsuario(usuario: Usuario) {
    this.loading.emit(true);
    this.usuarioService.obtenerUsuarioCompleto(usuario.username).subscribe(
      (usuarioCompleto: UsuarioCompleto) => {
        this.loading.emit(false);
        this.dialogService
          .open(ModificarUsuarioComponent, {
            closeOnBackdropClick: false,
            closeOnEsc: true,
            context: { usuario: usuarioCompleto },
          })
          .onClose.subscribe((response) => {
            if (response.usuario && response.accepted) {
              this.loading.emit(true);
              this.usuarioService
                .actualizarUsuarios(response.usuario)
                .subscribe(
                  (resp: UsuarioCompleto) => {
                    this.loading.emit(false);
                    this.toastrService.success(
                      'Se actualizó correctamente el usuario: ' +
                        resp.firstName,
                      'Actualización de usuario'
                    );

                    this.mapUsuarioCompletoToUsuario2(usuario,
                      resp,
                      response.tienda
                    );
                  },
                  (error) => {
                    this.loading.emit(false);
                    this.toastrService.danger(
                      'Ocurrió un error al actualizar el usuario: ' +
                        response.usuario.firstName,
                      'Actualización de usuario'
                    );
                  }
                );
            }
          });
      },
      (error) => {
        console.error(error);
        this.loading.emit(false);
      }
    );
  }

  eliminarUsuario(usuario: Usuario) {
    this.dialogService
      .open(GlobalAcceptanceComponent, {
        closeOnBackdropClick: false,
        closeOnEsc: true,
        context: {
          headerMessage: 'Eliminación de usuario',
          bodyMessage: '¿Desea eliminar el usuario: [' + usuario.nombre + ']?',
          acceptanceLabel: 'Si',
          cancelLabel: 'No',
        },
      })
      .onClose.subscribe((response) => {
        console.log(response);
        if (response.accept == true) {
          this.loading.emit(true);
          this.usuarioService.eliminarUsuario(usuario.username).subscribe(
            (response: any) => {
              if (response.response) {
                this.toastrService.success(
                  response.responseMessage,
                  'Eliminación'
                );
                this.registros = [...this.arrayRemove(this.registros, usuario)];

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
                'El usuario no se pudo eliminar',
                'Eliminación'
              );
              this.loading.emit(false);
            }
          );
        }
      });
  }

  mapUsuarioCompletoToUsuario(
    usuario: UsuarioCompleto,
    tienda: string
  ): Usuario {
    let usuarioSimple: Usuario = {
      id: usuario.id,
      nombre: usuario.name,
      rol: usuario.rol,
      username: usuario.user,
      correo: usuario.email,
      telefono: usuario.phoneNumber,
    };

    if (tienda) {
      usuarioSimple.tienda = tienda;
    }

    return usuarioSimple;
  }

  mapUsuarioCompletoToUsuario2(usuarioSimple: Usuario,
    usuario: UsuarioCompleto,
    tienda: string
  ) {
    
    usuarioSimple.id = usuario.id;
    usuarioSimple.nombre = usuario.name;
    usuarioSimple.rol = usuario.rol;
    usuarioSimple.username = usuario.user;
    usuarioSimple.correo = usuario.email;
    usuarioSimple.telefono = usuario.phoneNumber;
    
    if (tienda) {
      usuarioSimple.tienda = tienda;
    }
  }

  arrayRemove(arr: Usuario[], value: Usuario) {
    return arr.filter(function (ele) {
      return ele.id != value.id;
    });
  }
}
