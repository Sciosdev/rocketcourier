import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FiltroConsultaComponent } from './filtro-consulta.component';

describe('FiltroConsultaComponent', () => {
  let component: FiltroConsultaComponent;
  let fixture: ComponentFixture<FiltroConsultaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FiltroConsultaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FiltroConsultaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
