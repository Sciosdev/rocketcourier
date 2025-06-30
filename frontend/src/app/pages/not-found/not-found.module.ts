import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotFoundComponent } from './not-found.component';
import { NbButtonModule, NbCardModule } from '@nebular/theme';
import {MatCardModule} from '@angular/material/card';



@NgModule({
  declarations: [NotFoundComponent],
  imports: [
    CommonModule,
    NbCardModule,
    NbButtonModule,
    MatCardModule,
  ]
})
export class NotFoundModule { }
