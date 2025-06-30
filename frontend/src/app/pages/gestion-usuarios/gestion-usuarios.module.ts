import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FiltroConsultaComponent } from './filtro-consulta/filtro-consulta.component';
import { GestionUsuariosComponent } from './gestion-usuarios.component';
import { ThemeModule } from 'src/app/theme/theme.module';
import { ResultadoConsultaUsuariosComponent } from './resultado-consulta-usuarios/resultado-consulta-usuarios.component';
import { NgxLoadingXModule } from 'ngx-loading-x';
import { ModificarUsuarioComponent } from './popups/modificar-usuario/modificar-usuario.component';
import { PipesModule } from 'src/app/pipes/pipes.module';
import { AltaUsuarioComponent } from './popups/alta-usuario/alta-usuario.component';
import { FileUploadModule } from 'primeng/fileupload';
import { NbFormFieldModule, NbSelectModule } from '@nebular/theme';
import { MaterialModule } from 'src/app/material/material.module';

@NgModule({
  declarations: [FiltroConsultaComponent, GestionUsuariosComponent, ResultadoConsultaUsuariosComponent, ModificarUsuarioComponent, AltaUsuarioComponent],
  imports: [
    CommonModule,
    MaterialModule,
    ThemeModule,
    NgxLoadingXModule,
    PipesModule,
    FileUploadModule,
    NbSelectModule,
    NbFormFieldModule
  ]
})
export class GestionUsuariosModule { }
