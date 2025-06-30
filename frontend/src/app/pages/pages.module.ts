import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PagesRoutingModule } from './pages-routing.module';
import { PagesComponent } from './pages.component';
import { HomeModule } from './home/home.module';
import { NotFoundModule } from './not-found/not-found.module';

import {
  NbLayoutModule,
  NbSidebarModule,
  NbButtonModule,
  NbMenuModule,
  NbIconModule,
  NbCardModule,
} from '@nebular/theme';
import { NbEvaIconsModule } from '@nebular/eva-icons';
import { ThemeModule } from '../theme/theme.module';
import { CargaLayoutModule } from './carga-layout/carga-layout.module';
import { NgxLoadingXModule } from 'ngx-loading-x';
import { ConsultaInformacionModule } from './consulta-informacion/consulta-informacion.module';
import { NgxSpinnerModule } from 'ngx-spinner';
import { GestionUsuariosModule } from './gestion-usuarios/gestion-usuarios.module';
import { GestionTiendasModule } from './gestion-tiendas/gestion-tiendas.module';
import { ConfiguracionModule } from './configuracion/configuracion.module';
import { GlobalAcceptanceComponent } from './common-popups/global-acceptance/global-acceptance.component';

@NgModule({
  declarations: [PagesComponent, GlobalAcceptanceComponent],
  imports: [
    CommonModule,
    PagesRoutingModule,
    HomeModule,
    CargaLayoutModule,
    NotFoundModule,
    NbLayoutModule,
    NbSidebarModule,
    NbButtonModule,
    NbMenuModule,
    NbEvaIconsModule,
    NbIconModule,
    ThemeModule,
    NbCardModule,
    NgxLoadingXModule,
    ConsultaInformacionModule,
    NgxSpinnerModule,
    GestionUsuariosModule,
    GestionTiendasModule,
    ConfiguracionModule,

],
  providers: [],
  exports: []
})
export class PagesModule { }
