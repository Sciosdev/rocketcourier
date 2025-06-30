import { Component, EventEmitter, OnChanges, OnInit, Output } from '@angular/core';
import { FormControl, FormGroupDirective, NgForm, Validators } from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { NbStepperComponent } from '@nebular/theme';
import { TipoCargaService } from 'src/app/services/tipo-carga.service';
import Swal from 'sweetalert2';

  /** Error when invalid control is dirty, touched, or submitted. */
  export class MyErrorStateMatcher implements ErrorStateMatcher {
    isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
      const isSubmitted = form && form.submitted;
      return !!(control && control.invalid && (control.dirty || control.touched || isSubmitted));
    }
  }

@Component({
  selector: 'app-carga-archivo',
  templateUrl: './carga-archivo.component.html',
  styleUrls: ['./carga-archivo.component.scss']
})


export class CargaArchivoComponent implements OnInit, OnChanges {

  @Output() archivoCargado = new EventEmitter<any>();
  @Output() procesado = new EventEmitter<boolean>();
  @Output() tipoCarga = new EventEmitter<number>();

  /**
   * Nombre del archivo cargado
   */
  nombreArchivo = 'Ningún archivo seleccionado';

  matcher = new MyErrorStateMatcher();

  /**
   * Bandera que indica si se obtuvo un resultado
   */


  hasHeader: boolean;

  /**
   * Variable para saber si el salto de linea del archivo es CR
   */
  cr = false;
  /**
   * Variable para saber si el salto de linea del archivo es LF
   */
  lf = false;

  /**
   * Almacena el archvio CSV en JSON
   */
  resultJson: any[] = [];

  /**
   * Json copmo string
   */
  jsonAsText: string;

  fileLoaded:boolean;

  tipoCargaList: any[] = [];

  selectedTipoCarga: any;

  selected = new FormControl('valid', [Validators.required]);



  constructor(private stepper: NbStepperComponent,
    private tipoCargaService: TipoCargaService,
    ) {

  }

  siguiente(){
    this.nombreArchivo = 'Ningún archivo seleccionado';
    this.fileLoaded = false;
    this.stepper.next();
  }

  ngOnChanges(): void {

    if(!this.fileLoaded){
      this.nombreArchivo = 'Ningún archivo seleccionado';
    }
  }

  ngOnInit() {
    this.hasHeader = false;
    this.fileLoaded = false;
    //console.log("OnInit");

    this.tipoCargaService.obtenerTipoCarga().subscribe((response: any[]) => {
      this.tipoCargaList = response;
    });
  }



  setHasHeader(hasHeader: boolean) {
    this.hasHeader = hasHeader;
  }

  readExcel(event) {

    if (!event.target.files[0]) {
      this.resultJson = [];
      this.nombreArchivo = 'Ningún archivo seleccionado';
      console.error("no se cargo el archivo")
      Swal.fire('Error al cargar', 'Ocurrió un error al cargar el archivo', 'error');
      return;
    }

    const target: DataTransfer = <DataTransfer>(event.target);

    if (target.files.length !== 1) {
      this.resultJson = [];
      this.nombreArchivo = 'Ningún archivo seleccionado';
      Swal.fire('Error al cargar', 'No se puede cargar más de un archivo', 'error');
      throw new Error('Cannot use multiple files');
    }

    console.log(target.files[0].type);

    this.fileLoaded = true;
    this.nombreArchivo = target.files[0].name;
    this.archivoCargado.emit(target.files[0]);
    this.procesado.emit(false);
    this.tipoCarga.emit(this.selectedTipoCarga);

    console.log(this.selectedTipoCarga);

    event.target.value = '';
  }

}
