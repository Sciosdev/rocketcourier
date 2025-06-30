import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultadoConsultaTiendasComponent } from './resultado-consulta-tiendas.component';

describe('ResultadoConsultaTiendasComponent', () => {
  let component: ResultadoConsultaTiendasComponent;
  let fixture: ComponentFixture<ResultadoConsultaTiendasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResultadoConsultaTiendasComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultadoConsultaTiendasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
