import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router'; // we also need angular router for Nebular to function properly
import { NbSidebarModule, NbLayoutModule, NbButtonModule, NbCardModule, NbProgressBarModule, NbSpinnerModule, NbIconModule } from '@nebular/theme';
import { HomeComponent } from './home.component';
import { FormsModule } from '@angular/forms';
import { MaterialModule } from '../../material/material.module';
import {ButtonModule} from 'primeng/button';
import { ThemeModule } from 'src/app/theme/theme.module';
import { NbEvaIconsModule } from '@nebular/eva-icons';
import {TimelineModule} from 'primeng/timeline';
import { CardModule } from "primeng/card";
import { NgxLoadingXModule } from 'ngx-loading-x';
import {DividerModule} from 'primeng/divider';
import {ScrollPanelModule} from 'primeng/scrollpanel';

@NgModule({
  declarations: [HomeComponent],
  imports: [
    CommonModule,
    RouterModule, // RouterModule.forRoot(routes, { useHash: true }), if this is your app.module
    NbLayoutModule,
    NbSidebarModule, // NbSidebarModule.forRoot(), //if this is your app.module
    NbButtonModule,
    NbCardModule,
    NbProgressBarModule,
    CommonModule,
    NbButtonModule,
    NbCardModule,
    FormsModule,
    MaterialModule,
    NbSpinnerModule,
    NbEvaIconsModule,
    NbIconModule,
    ThemeModule,
    ButtonModule,
    TimelineModule,
    CardModule,
    NgxLoadingXModule,
    DividerModule,
    ScrollPanelModule
  ]
})
export class HomeModule { }
