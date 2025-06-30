import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionTiendasComponent } from './gestion-tiendas.component';

describe('GestionTiendasComponent', () => {
  let component: GestionTiendasComponent;
  let fixture: ComponentFixture<GestionTiendasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GestionTiendasComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GestionTiendasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
