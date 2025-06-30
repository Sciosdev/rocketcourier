import { Component } from '@angular/core';
import { NbRegisterComponent } from '@nebular/auth';
import Swal from 'sweetalert2';

/**
 * Clase para gestionar el registro de usuarios definido en @nebular/auth
 */
@Component({
  selector: 'portal-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent extends NbRegisterComponent {
  /**
   * Propiedad que almacena el titulo del mensaje de error en caso de existir
   */
  titulo: string = '';
  /**
   * Método para el registro de usuarios
   */
  register() {
    const _this = this;
    this.errors = this.messages = [];
    this.submitted = true;
    this.service
      .register(this.strategy, this.user)
      .pipe()
      .subscribe(result => {
        if (result.getResponse().status === 0)
          Swal.fire(
            'Error',
            'No se pudo conectar con el servidor, intente de nuevo por favor.',
            'error',
          );
        else {
          _this.submitted = false;

          if (result.isSuccess()) {
            _this.messages = result.getMessages();
          } else {
            _this.errors = result.getErrors();
            _this.errors.pop();

            this.titulo = result.getResponse().error.mensaje;

            let mensaje: string = result.getResponse().error.errors.message;
            const _mensaje: string = result.getResponse().error.errors._message;
            mensaje = mensaje.replace(_mensaje + ':', '');
            _this.errors.push(mensaje);
          }

          const redirect = result.getRedirect();
          if (redirect) {
            setTimeout(function() {
              return _this.router.navigateByUrl(redirect);
            }, _this.redirectDelay);
          }
          _this.cd.detectChanges();
        }
      });
  }
}
