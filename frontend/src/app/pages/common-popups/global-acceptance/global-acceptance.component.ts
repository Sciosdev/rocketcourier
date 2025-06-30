import { Component, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

@Component({
  selector: 'app-global-acceptance',
  templateUrl: './global-acceptance.component.html',
  styleUrls: ['./global-acceptance.component.scss']
})
export class GlobalAcceptanceComponent implements OnInit {

  headerMessage;
  bodyMessage;
  acceptanceLabel;
  cancelLabel;

  constructor(protected ref: NbDialogRef<GlobalAcceptanceComponent>) { }

  ngOnInit(): void {
  }

  accept(){
    this.ref.close({accept: true});
  }

  cancel(){
    this.ref.close({accept: false});
  }

}
