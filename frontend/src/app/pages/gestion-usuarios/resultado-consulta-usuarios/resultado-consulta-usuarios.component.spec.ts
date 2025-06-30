import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultadoConsultaUsuariosComponent } from './resultado-consulta-usuarios.component';

describe('ResultadoConsultaUsuariosComponent', () => {
  let component: ResultadoConsultaUsuariosComponent;
  let fixture: ComponentFixture<ResultadoConsultaUsuariosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResultadoConsultaUsuariosComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultadoConsultaUsuariosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
