import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GestionTiendasComponent } from './gestion-tiendas.component';
import { FiltroConsultaTiendasComponent } from './filtro-consulta-tiendas/filtro-consulta-tiendas.component';
import { ResultadoConsultaTiendasComponent } from './resultado-consulta-tiendas/resultado-consulta-tiendas.component';
import { NgxLoadingXModule } from 'ngx-loading-x';
import { ThemeModule } from 'src/app/theme/theme.module';
import { MaterialModule } from 'src/app/material/material.module';
import { AltaTiendaComponent } from './popups/alta-tienda/alta-tienda.component';
import { ModificacionTiendaComponent } from './popups/modificacion-tienda/modificacion-tienda.component';
import { PipesModule } from 'src/app/pipes/pipes.module';
import {FileUploadModule} from 'primeng/fileupload';
import {HttpClientModule} from '@angular/common/http';
import {BlockUIModule} from 'primeng/blockui';

@NgModule({
  declarations: [GestionTiendasComponent, FiltroConsultaTiendasComponent, ResultadoConsultaTiendasComponent, AltaTiendaComponent, ModificacionTiendaComponent],
  imports: [
    CommonModule,
    MaterialModule,
    ThemeModule,
    NgxLoadingXModule,
    PipesModule,
    FileUploadModule,
    HttpClientModule,
    BlockUIModule
  ]
})

export class GestionTiendasModule { }
