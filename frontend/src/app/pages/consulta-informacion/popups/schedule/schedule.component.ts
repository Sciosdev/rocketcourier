import { Component, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { RegistroTable } from 'src/app/models/registro.table.model';

@Component({
  selector: 'app-schedule',
  templateUrl: './schedule.component.html',
  styleUrls: ['./schedule.component.scss']
})
export class ScheduleComponent implements OnInit {
  comentario: String;
  selectedDate: Date;
  today: Date;
  row: RegistroTable;
  disabled: boolean = false;

  constructor(protected ref: NbDialogRef<ScheduleComponent>) { }

  ngOnInit(): void {

    if (this.row) {
      this.selectedDate = new Date(Date.parse(this.row.scheduledDt.toString()));
      this.comentario = this.row.comment;
    } else {
      this.selectedDate = new Date();
      this.selectedDate.setSeconds(0);
    }

    this.today = new Date();
  }

  cancel() {
    this.ref.close();
  }

  submit(value: any) {
    this.ref.close(value);
  }
}
