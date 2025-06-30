import { ModuleWithProviders, NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_RIPPLE_GLOBAL_OPTIONS } from '@angular/material/core';
import { RippleService } from './utils/ripple.service';
import { NbPasswordAuthStrategy, NbAuthModule } from '@nebular/auth';

export const NB_CORE_PROVIDERS = [
  ...NbAuthModule.forRoot({

    strategies: [
      NbPasswordAuthStrategy.setup({
        name: 'email',
      }),
    ],
    forms: {
      login: {
        
      },
      register: {
        
      },
    },
  }).providers,]

@NgModule({
  imports: [
    CommonModule,
  ],

  declarations: [],
})
export class CoreModule {
 
  static forRoot(): ModuleWithProviders<CoreModule> {
    return {
      ngModule: CoreModule,
      providers: [
        ...NB_CORE_PROVIDERS,
        {provide: MAT_RIPPLE_GLOBAL_OPTIONS, useExisting: RippleService}
      ],
    };
  }
}
