import { Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { NbDialogRef } from '@nebular/theme';
import { UsuarioCompleto } from 'src/app/models/usuario-completo.model';
import { RolesService } from 'src/app/services/roles.service';
import { TiendaService } from 'src/app/services/tienda.service';
import { RocketPatterns } from '../../../../utils/patterns';

@Component({
  selector: 'app-alta-usuario',
  templateUrl: './alta-usuario.component.html',
  styleUrls: ['./alta-usuario.component.scss'],
})
export class AltaUsuarioComponent implements OnInit {
  usuario: UsuarioCompleto;
  blocked: boolean = false;
  contents: any = null;
  filename: string;
  imagePath;

  roles_combo: any[] = [];
  tiendas_combo: any[] = [];

  selectedTienda;
  selectedRol;
  confirmapassword;

  selectedRolFormControl = new FormControl('', Validators.required);
  selectedTiendaFormControl = new FormControl('', Validators.required);

  emailPattern = RocketPatterns.email;
  passwordPattern = RocketPatterns.password;
  namePattern = RocketPatterns.personalName;
  zipPattern = RocketPatterns.zipCode;
  phoneNumberPattern = RocketPatterns.phoneNumber;
  usernamePattern = RocketPatterns.username;

  showPassword = false;
  showCPassword = false;

  constructor(
    protected ref: NbDialogRef<AltaUsuarioComponent>,
    private _sanitizer: DomSanitizer,
    private rolesService: RolesService,
    private tiendaService: TiendaService
  ) {}

  ngOnInit(): void {
    this.usuario = new UsuarioCompleto();

    this.rolesService.obtenerRoles().subscribe((roles: any[]) => {
      this.roles_combo = roles;
    });

    this.tiendaService.obtenerCatalogoTiendas().subscribe((tiendas: any[]) => {
      this.tiendas_combo = tiendas;
    });
  }

  accept() {
    this.usuario.name = '';

    if (this.usuario.firstName)
      this.usuario.name = this.usuario.name.concat(this.usuario.firstName);

    if (this.usuario.lastName)
      this.usuario.name = this.usuario.name.concat(' ', this.usuario.lastName);

    if (this.usuario.secondLastName)
      this.usuario.name = this.usuario.name.concat(
        ' ',
        this.usuario.secondLastName
      );

    this.usuario.rol = this.selectedRolFormControl.value.rol;

    let nombreTienda: string = null;

    if (this.selectedRolFormControl.value.vendorAssignment) {
      if (this.selectedTienda) {
        this.usuario.tienda = this.selectedTienda;
        this.tiendas_combo.forEach((tienda) => {
          if (tienda.id == this.selectedTienda) {
            nombreTienda = tienda.nombreTienda;
          }
        });
      }
    }

    this.ref.close({
      accept: true,
      usuario: this.usuario,
      tienda: nombreTienda,
    });
  }

  cancel() {
    this.ref.close({ accept: false });
  }

  myUploader(event, form) {
    for (const file of event.files) {
      this.readFile(file);
    }
    form.clear();
  }

  onBeforeUpload() {
    this.blocked = true;
  }

  onUpload() {
    this.blocked = false;
  }

  private readFile(file: File) {
    const reader: FileReader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => {
      let data: any = reader.result;
      this.usuario.foto = data.split(',')[1];

      this.imagePath = this._sanitizer.bypassSecurityTrustResourceUrl(
        'data:image/jpg;base64,' + this.usuario.foto
      );
    };
  }

  getInputType() {
    if (this.showPassword) {
      return 'text';
    }
    return 'password';
  }

  toggleShowPassword() {
    this.showPassword = !this.showPassword;
  }

  getCInputType() {
    if (this.showCPassword) {
      return 'text';
    }
    return 'password';
  }

  toggleShowCPassword() {
    this.showCPassword = !this.showCPassword;
  }
}
