import { TestBed } from '@angular/core/testing';

import { TipoCargaService } from './tipo-carga.service';

describe('TipoCargaService', () => {
  let service: TipoCargaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TipoCargaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
