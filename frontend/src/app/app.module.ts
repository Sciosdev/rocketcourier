import { BrowserModule } from '@angular/platform-browser';
import { LOCALE_ID, NgModule } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NbLayoutModule, NbSidebarModule, NbMenuModule } from '@nebular/theme';
import { NbEvaIconsModule } from '@nebular/eva-icons';
import { HttpClientModule, HttpRequest, HTTP_INTERCEPTORS } from '@angular/common/http';
import { MaterialModule } from './material/material.module';
import { ThemeModule } from './theme/theme.module';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import {
  NbAuthModule,
  NB_AUTH_TOKEN_INTERCEPTOR_FILTER,
  NbOAuth2AuthStrategy,
  NbOAuth2GrantType,
  NbOAuth2ClientAuthMethod,
  NbAuthOAuth2JWTToken,
} from '@nebular/auth';
import { AuthGuard } from './auth.guard';

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NbRoleProvider, NbSecurityModule } from '@nebular/security';
import { RoleProvider } from './auth/role.provider';
import { NgxJsonViewerModule } from 'ngx-json-viewer';
import { NgxLoadingXConfig, NgxLoadingXModule, POSITION, SPINNER } from 'ngx-loading-x';
import { NbAuthJWTInterceptor } from './services/interceptors/NbAuthJWTInterceptot';
import { environment } from 'src/environments/environment';

import es from '@angular/common/locales/es';
import { registerLocaleData } from '@angular/common';
import { getLatamPaginatorIntl } from './paginator/mx-paginator.intl';
import { MatPaginatorIntl } from '@angular/material/paginator';

const ngxLoadingXConfig: NgxLoadingXConfig = {
  show: false,
  bgBlur: 2,
  bgColor: 'rgba(40, 40, 40, 0.5)',
  bgOpacity: 5,
  bgLogoUrl: 'assets/images/water_mark.png',
  bgLogoUrlPosition: POSITION.bottomRight,
  bgLogoUrlSize: 100,
  spinnerType: SPINNER.wanderingCubes,
  spinnerSize: 100,
  spinnerColor: '#ff7010',
  spinnerPosition: POSITION.centerCenter,
}

registerLocaleData(es);

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    NgxLoadingXModule.forRoot(ngxLoadingXConfig),
    NgxJsonViewerModule,
    HttpClientModule,
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    NbSidebarModule.forRoot(),
    NbMenuModule.forRoot(),
    NbLayoutModule,
    NbEvaIconsModule,
    MaterialModule,
    ThemeModule,
    NgbModule,
    NbAuthModule.forRoot({
      strategies: [NbOAuth2AuthStrategy.setup({
        name: 'oauth',
        clientId: environment.clientId,
        clientSecret: environment.clientSecret,
        baseEndpoint: environment.endpoint,
        clientAuthMethod: NbOAuth2ClientAuthMethod.BASIC,
        token: {
          endpoint: '/oauth/token',
          class: NbAuthOAuth2JWTToken,
          grantType: NbOAuth2GrantType.PASSWORD
        },
        refresh: {
          endpoint: '/oauth/token',
          grantType: NbOAuth2GrantType.REFRESH_TOKEN,
        },
        redirect: {
          success: '/intranet/inicio',
          failure: '/auth/login'
        },
      }),
      ],
      forms: {
        login: {
          strategy: 'oauth'
        },
        logout: {
          redirectDelay: 200,
          strategy: 'oauth',
        },
        register: {
          redirectDelay: 500,
        },

      },
    }),
    FontAwesomeModule,
    NbSecurityModule.forRoot({
      accessControl: {
        guest: {
          menu: ['guest'],
          view: ['guest'],
        },
        messenger: {
          parent: 'guest',
          menu: ['messenger'],
          view: ['messenger'],
          filtro: ['messenger']
        },
        customer: {
          parent: 'guest',
          menu: ['customer'],
          view: ['customer'],
          filtro: ['customer']
        },
        courier: {
          parent: 'guest',
          menu: ['courier'],
          view: ['courier'],
          filtro: ['courier']
        },
        admin: {
          parent: 'guest',
          menu: ['admin'],
          view: ['admin'],
          filtro: ['admin']
        },
        full: {
          parent: 'admin',
        },
      }
    }),

  ],
  providers: [ThemeModule.forRoot().providers,
    AuthGuard,
  { provide: NbRoleProvider, useClass: RoleProvider },
  { provide: HTTP_INTERCEPTORS, useClass: NbAuthJWTInterceptor, multi: true },
  {
    provide: NB_AUTH_TOKEN_INTERCEPTOR_FILTER,
    useValue: function (req: HttpRequest<any>) {
      //console.log(req.url);
      if (req.url === environment.endpoint + '/auth/login' || 
      req.url === environment.endpoint + '/oauth/token' || 
      req.url === environment.endpoint + '/oauth/authorize') {

        return true;
      }
      return false;
    },
  },
  { provide: LOCALE_ID, useValue: 'es-MX' },
  { provide: MatPaginatorIntl, useValue: getLatamPaginatorIntl() }
  ],
  bootstrap: [AppComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class AppModule { }