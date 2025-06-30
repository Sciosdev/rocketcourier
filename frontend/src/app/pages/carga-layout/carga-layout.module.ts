import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CargaLayoutComponent } from './carga-layout.component';
import { ThemeModule } from '../../theme/theme.module';
import { MaterialModule } from '../../material/material.module';
import { CargaArchivoComponent } from './carga-archivo/carga-archivo.component';
import { PrevisualizacionComponent } from './previsualizacion/previsualizacion.component';
import { PrettyPrintJsonPipe } from 'src/app/pipes/pretty-print-json.pipe';
import { NgxJsonViewerModule } from 'ngx-json-viewer';
import {TableModule} from 'primeng/table';
import { NgxLoadingXModule } from 'ngx-loading-x';
import { ShowResultComponent } from './show-result/show-result.component';



@NgModule({
  declarations: [CargaLayoutComponent, CargaArchivoComponent, PrevisualizacionComponent, PrettyPrintJsonPipe, ShowResultComponent],
  imports: [
    CommonModule,
    ThemeModule,
    MaterialModule,
    NgxJsonViewerModule,
    TableModule,
    NgxLoadingXModule
  ]
})
export class CargaLayoutModule { }
