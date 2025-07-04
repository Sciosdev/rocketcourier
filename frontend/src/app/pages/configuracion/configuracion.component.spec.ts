import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { NbToastrService } from '@nebular/theme';
import { NbAuthService } from '@nebular/auth';
import { ConfiguracionComponent } from './configuracion.component';
import { TiendaService } from '../../services/tienda.service';
import { UsuarioService } from 'src/app/services/usuario.service';

class TiendaServiceStub {
  actualizarCredencialesShopify() {
    return of({});
  }
}

class ToastrServiceStub {
  success(message?: string, title?: string) {}
  danger(message?: string, title?: string) {}
}

describe('ConfiguracionComponent', () => {
  let component: ConfiguracionComponent;
  let fixture: ComponentFixture<ConfiguracionComponent>;
  let tiendaService: TiendaServiceStub;
  let toastrService: ToastrServiceStub;

  beforeEach(async () => {
    tiendaService = new TiendaServiceStub();
    toastrService = new ToastrServiceStub();

    await TestBed.configureTestingModule({
      declarations: [ConfiguracionComponent],
      providers: [
        { provide: TiendaService, useValue: tiendaService },
        {
          provide: NbAuthService,
          useValue: { getToken: () => of({ isValid: () => false }) },
        },
        { provide: UsuarioService, useValue: {} },
        { provide: NbToastrService, useValue: toastrService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfiguracionComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should show success message on guardar success', () => {
    const spy = spyOn(tiendaService, 'actualizarCredencialesShopify').and.returnValue(of({}));
    const toastSpy = spyOn(toastrService, 'success');
    component.vendorId = 1;
    component.guardar();
    expect(spy).toHaveBeenCalled();
    expect(toastSpy).toHaveBeenCalled();
    expect(component.saving).toBeFalse();
  });

  it('should show error message on guardar failure', () => {
    spyOn(tiendaService, 'actualizarCredencialesShopify').and.returnValue(throwError(() => new Error('fail')));
    const toastSpy = spyOn(toastrService, 'danger');
    const logSpy = spyOn(console, 'error');
    component.vendorId = 1;
    component.guardar();
    expect(toastSpy).toHaveBeenCalled();
    expect(logSpy).toHaveBeenCalled();
    expect(component.saving).toBeFalse();
  });
});
