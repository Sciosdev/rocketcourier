import { Component, OnInit, Inject, OnDestroy } from '@angular/core';
import {
  NbLogoutComponent,
  NbAuthService,
  NB_AUTH_OPTIONS,
  NbTokenService,
  NbAuthResult,
  NbAuthOAuth2JWTToken,
} from '@nebular/auth';
import { Router } from '@angular/router';
import { UsuarioService } from '../../services/usuario.service';
import { delay, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';

/**
 * Clase para el cierre de sesión definido en @nebular/auth
 */
@Component({
  selector: 'portal-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss'],
})

export class LogoutComponent extends NbLogoutComponent  {

/**
 * Constructor de la clase
 */
 token: NbAuthOAuth2JWTToken;

  constructor(
    protected authService: NbAuthService,
    @Inject(NB_AUTH_OPTIONS) protected options = {},
    protected router: Router,
    protected tokenService: NbTokenService,
    protected usuarioService: UsuarioService,
  ) {
    super(authService, Option, router);
    this.redirectDelay = this.getConfigValue('forms.logout.redirectDelay');
    this.strategy = this.getConfigValue('forms.logout.strategy');

    this.authService.onTokenChange()
      .subscribe((token: NbAuthOAuth2JWTToken) => {
        this.token = null;
        if (token && token.isValid()) {
          this.token = token;
        }
      });
  }

  logout() {
    this.authService.logout(this.strategy)
      .pipe(
        delay(this.redirectDelay),
      )
      .subscribe((result: NbAuthResult) => {
        console.debug(result);
        this.router.navigate(['/intranet/inicio'])
      });
  }
}
