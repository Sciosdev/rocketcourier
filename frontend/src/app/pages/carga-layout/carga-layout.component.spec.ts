import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CargaLayoutComponent } from './carga-layout.component';

describe('CargaLayoutComponent', () => {
  let component: CargaLayoutComponent;
  let fixture: ComponentFixture<CargaLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CargaLayoutComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CargaLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
