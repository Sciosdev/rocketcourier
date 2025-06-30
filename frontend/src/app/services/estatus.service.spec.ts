import { TestBed } from '@angular/core/testing';

import { EstatusService } from './estatus.service';

describe('EstatusService', () => {
  let service: EstatusService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EstatusService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
