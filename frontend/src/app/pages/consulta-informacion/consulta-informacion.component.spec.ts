import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaInformacionComponent } from './consulta-informacion.component';

describe('ConsultaInformacionComponent', () => {
  let component: ConsultaInformacionComponent;
  let fixture: ComponentFixture<ConsultaInformacionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConsultaInformacionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConsultaInformacionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
