import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModificacionTiendaComponent } from './modificacion-tienda.component';

describe('ModificacionTiendaComponent', () => {
  let component: ModificacionTiendaComponent;
  let fixture: ComponentFixture<ModificacionTiendaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ModificacionTiendaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ModificacionTiendaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
