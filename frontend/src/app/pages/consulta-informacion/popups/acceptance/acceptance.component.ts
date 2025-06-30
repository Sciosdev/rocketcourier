import { Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { UsuarioService } from 'src/app/services/usuario.service';

@Component({
  selector: 'app-acceptance',
  templateUrl: './acceptance.component.html',
  styleUrls: ['./acceptance.component.scss']
})
export class AcceptanceComponent implements OnInit {

  selectedCourier: any;
  courierFormControl = new FormControl('', Validators.required);
  couriers: any[] = [];
  
  constructor(protected ref: NbDialogRef<AcceptanceComponent>,
    private usuarioService: UsuarioService) { }

  ngOnInit(): void {
    this.usuarioService.obtenerCouriers().subscribe(
      (resp: any[]) => {
        resp.forEach(element => {
          this.couriers.push(element);
        })
      }
    );

  }

  accept() {
    this.ref.close({accepted: true, courier: this.selectedCourier});
  }

  cancel() {
    this.ref.close({accepted: false});
  }

}
