import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CambioEstatusMultipleComponent } from './cambio-estatus-multiple.component';

describe('CambioEstatusMultipleComponent', () => {
  let component: CambioEstatusMultipleComponent;
  let fixture: ComponentFixture<CambioEstatusMultipleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CambioEstatusMultipleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CambioEstatusMultipleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
