import { Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Estatus } from 'src/app/models/estatus.model';
import { TipoEstatus } from 'src/app/models/tipo.estatus.model';
import { EstatusService } from 'src/app/services/estatus.service';
import { UsuarioService } from 'src/app/services/usuario.service';

@Component({
  selector: 'app-cambio-estatus-multiple',
  templateUrl: './cambio-estatus-multiple.component.html',
  styleUrls: ['./cambio-estatus-multiple.component.scss'],
})
export class CambioEstatusMultipleComponent implements OnInit {
  
  currentEstatus: Estatus;
  selectedEstatus: Estatus;
  estatusFormControl = new FormControl('', Validators.required);
  estatusList: Estatus[] = [];
  selectedCourier: any;
  courierFormControl = new FormControl('', Validators.required);
  courierList: any[] = [];

  totalRegistros: number;
  enableFinalComment: boolean = false;
  comentario: string;

  constructor(
    protected ref: NbDialogRef<CambioEstatusMultipleComponent>,
    protected estatusService: EstatusService,
    protected usuarioService: UsuarioService
  ) {}

  ngOnInit(): void {
    this.estatusList = [];
    this.estatusService
      .obtenerEstatusSiguiente(this.currentEstatus.id)
      .subscribe((result: Estatus) => {
        if(result.tipo === TipoEstatus.FINAL){
          this.enableFinalComment = true;
        }
        this.estatusList.push(result);
      });

    this.estatusService
      .obtenerEstatusSiguienteException(this.currentEstatus.id)
      .subscribe(
        (result: Estatus) => {
          if (result) this.estatusList.push(result);
        },
        (error) => {}
      );

    if (!this.currentEstatus) {
      this.currentEstatus = {
        desc: 'Sin estatus',
        id: -1,
        siguiente: 0,
        siguienteError: 0,
        tipo: TipoEstatus.EXCEPCION,
      };
    }

    this.courierList = [];

    this.usuarioService.obtenerCouriers().subscribe(
      (response: any[]) => {
        this.courierList = response;
      },
      (error) => {
        console.warn(error);
      }
    );
  }

  accept() {
    this.ref.close({
      estatus: this.selectedEstatus,
      courier: this.selectedCourier,
      comment: this.comentario
    });
  }

  cancel() {
    this.ref.close();
  }

  activateReassignCourier(): boolean {
    if (this.currentEstatus.tipo === TipoEstatus.REASIGNACION) {
      return true;
    }

    return false;
  }
}
