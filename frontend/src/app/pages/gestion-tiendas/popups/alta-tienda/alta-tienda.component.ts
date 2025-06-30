import { Component, OnInit } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { NbDialogRef } from '@nebular/theme';
import { Tienda } from 'src/app/models/tienda.model';
import { RocketPatterns } from 'src/app/utils/patterns';

@Component({
  selector: 'app-alta-tienda',
  templateUrl: './alta-tienda.component.html',
  styleUrls: ['./alta-tienda.component.scss']
})
export class AltaTiendaComponent implements OnInit {

  constructor(
    protected ref: NbDialogRef<AltaTiendaComponent>,
    private _sanitizer: DomSanitizer
  ) {}

  tienda: Tienda;
  blocked: boolean = false;
  contents: any = null;
  filename: string;

  imagePath;
  
  emailPattern = RocketPatterns.email;
  phoneNumberPattern = RocketPatterns.phoneNumber;
  zipPattern = RocketPatterns.zipCode;

  ngOnInit(): void {
    this.tienda = new Tienda();
  }

  accept() {
    this.ref.close({ accept: true, tienda: this.tienda });
  }

  cancel() {
    this.ref.close({ accept: false });
  }

  myUploader(event, form) {
    for (const file of event.files) {
      const dataset = this.readFile(file);
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
      this.tienda.logo = data.split(',')[1];

      this.imagePath = this._sanitizer.bypassSecurityTrustResourceUrl(
       'data:image/jpg;base64,' + this.tienda.logo
      );
    };
  }
}
