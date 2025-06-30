import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CambioEstatusComponent } from './cambio-estatus.component';

describe('CambioEstatusComponent', () => {
  let component: CambioEstatusComponent;
  let fixture: ComponentFixture<CambioEstatusComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CambioEstatusComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CambioEstatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
