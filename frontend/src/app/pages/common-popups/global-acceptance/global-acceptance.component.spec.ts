import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GlobalAcceptanceComponent } from './global-acceptance.component';

describe('GlobalAcceptanceComponent', () => {
  let component: GlobalAcceptanceComponent;
  let fixture: ComponentFixture<GlobalAcceptanceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GlobalAcceptanceComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GlobalAcceptanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
