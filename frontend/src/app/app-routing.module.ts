import { NgModule } from '@angular/core';
import { Routes, RouterModule, ExtraOptions } from '@angular/router';


const routes: Routes = [
  {
    path: 'intranet',
    loadChildren: () => import('./pages/pages.module')
      .then(m => m.PagesModule),
      
  },
  { path: 'auth', 
    loadChildren: () => import('./auth/auth.module')
      .then(m => m.AuthModule) 
  },
  { path: '', redirectTo: 'intranet', pathMatch: 'full' },
  { path: '**', redirectTo: 'intranet' },
];

const config: ExtraOptions = {
  useHash: false,
};

@NgModule({
  imports: [RouterModule.forRoot(routes, config)],
  exports: [RouterModule],
})

export class AppRoutingModule { }