import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UndefinedPipe } from './undefined.pipe';



@NgModule({
  declarations: [UndefinedPipe],
  imports: [
    CommonModule
  ],
  exports: [UndefinedPipe]
})
export class PipesModule { }
