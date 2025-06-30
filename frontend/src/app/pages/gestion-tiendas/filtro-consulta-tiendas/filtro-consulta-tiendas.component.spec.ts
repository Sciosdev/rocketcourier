import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FiltroConsultaTiendasComponent } from './filtro-consulta-tiendas.component';

describe('FiltroConsultaTiendasComponent', () => {
  let component: FiltroConsultaTiendasComponent;
  let fixture: ComponentFixture<FiltroConsultaTiendasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FiltroConsultaTiendasComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FiltroConsultaTiendasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
