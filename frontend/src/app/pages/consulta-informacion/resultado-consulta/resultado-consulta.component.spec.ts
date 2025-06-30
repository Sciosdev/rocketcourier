import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultadoConsultaComponent } from './resultado-consulta.component';

describe('ResultadoConsultaComponent', () => {
  let component: ResultadoConsultaComponent;
  let fixture: ComponentFixture<ResultadoConsultaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResultadoConsultaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultadoConsultaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
