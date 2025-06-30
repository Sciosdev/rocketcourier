import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AltaTiendaComponent } from './alta-tienda.component';

describe('AltaTiendaComponent', () => {
  let component: AltaTiendaComponent;
  let fixture: ComponentFixture<AltaTiendaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AltaTiendaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AltaTiendaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
