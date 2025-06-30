import { Component, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { Tienda } from 'src/app/models/tienda.model';
import { DomSanitizer } from '@angular/platform-browser';
import { RocketPatterns } from 'src/app/utils/patterns';

@Component({
  selector: 'app-modificacion-tienda',
  templateUrl: './modificacion-tienda.component.html',
  styleUrls: ['./modificacion-tienda.component.scss'],
})
export class ModificacionTiendaComponent implements OnInit {
  constructor(
    protected ref: NbDialogRef<ModificacionTiendaComponent>,
    private _sanitizer: DomSanitizer
  ) {}

  tienda: Tienda;
  mtienda: Tienda;
  blocked: boolean = false;
  contents: any = null;
  filename: string;

  imagePath;

  emailPattern = RocketPatterns.email;
  phoneNumberPattern = RocketPatterns.phoneNumber;
  zipPattern = RocketPatterns.zipCode;
  
  ngOnInit(): void {
    this.mtienda = new Tienda();
    this.mtienda.setTienda(this.tienda);

    if(this.mtienda.direccionCompleta == undefined) {
      this.mtienda.direccionCompleta = {};
    }
    
    this.imagePath = this._sanitizer.bypassSecurityTrustResourceUrl(
      'data:image/jpg;base64,' + this.mtienda.logo
    );
  }

  accept() {
    this.ref.close({ accept: true, tienda: this.mtienda });
  }

  cancel() {
    this.ref.close({ accept: false });
  }

  myUploader(event, form) {
    console.log('Reading file...');
    for (const file of event.files) {
      const dataset = this.readFile(file);
      console.log('onUpload: ', dataset);
    }
    form.clear();
  }

  onBeforeUpload() {
    this.blocked = true;
  }

  onUpload() {
    this.blocked = false;
  }

  deleteLogo(){
    this.mtienda.logo = "nologo";
  }

  private readFile(file: File) {
    const reader: FileReader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => {
      let data: any = reader.result;
      this.mtienda.logo = data.split(',')[1];

      this.imagePath = this._sanitizer.bypassSecurityTrustResourceUrl(
       'data:image/jpg;base64,' + this.mtienda.logo
      );
    };
  }
}
