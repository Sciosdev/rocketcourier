import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NbCardModule, NbButtonModule, NbInputModule } from '@nebular/theme';
import { MaterialModule } from 'src/app/material/material.module';
import { ThemeModule } from 'src/app/theme/theme.module';
import { ConfiguracionComponent } from './configuracion.component';

@NgModule({
  declarations: [ConfiguracionComponent],
  imports: [
    CommonModule,
    FormsModule,
    NbCardModule,
    NbButtonModule,
    NbInputModule,
    MaterialModule,
    ThemeModule
  ]
})
export class ConfiguracionModule { }
